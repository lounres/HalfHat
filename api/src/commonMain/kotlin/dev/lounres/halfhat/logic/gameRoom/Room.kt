package dev.lounres.halfhat.logic.gameRoom

import dev.lounres.halfhat.logic.gameStateMachine.*
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.algebraic.order
import dev.lounres.kone.automata.*
import dev.lounres.kone.collections.array.KoneUIntArray
import dev.lounres.kone.collections.array.generate
import dev.lounres.kone.collections.array.toKoneUIntArray
import dev.lounres.kone.collections.interop.toKoneUIntArray
import dev.lounres.kone.collections.iterables.isNotEmpty
import dev.lounres.kone.collections.list.*
import dev.lounres.kone.collections.list.implementations.KoneGCLinkedSizedList
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.map.of
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.utils.*
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.relations.*
import dev.lounres.kone.scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.Serializable
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


public class Room<
    RoomMetadata,
    PlayerID,
    PlayerMetadata : Room.Player.Metadata<PlayerID>,
    WordsProviderID,
    WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
    NoWordsProviderReason,
    ConnectionType: Room.Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>,
>(
    public val metadata: RoomMetadata,
    public val wordsProviderRegistry: WordsProviderRegistry<WordsProviderID, WordsProviderDescription, NoWordsProviderReason>,
    initialPlayersList: KoneList<PlayerMetadata>,
    initialGlobalRoles: KoneList<GlobalRole>,
    initialSettingsBuilder: GameSettings.Builder<WordsProviderDescription>,
    initialExtraSettings: ExtraSettings,
    private val initialMetadataFactory: (PlayerID) -> PlayerMetadata,
    private val checkConnectionAttachment: (metadata: PlayerMetadata, isOnline: Boolean, connection: ConnectionType) -> Boolean,
) {
    public class Player<
        RoomMetadata,
        PlayerID,
        PlayerMetadata : Player.Metadata<PlayerID>,
        WordsProviderID,
        WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
        NoWordsProviderReason,
        ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>
    > internal constructor(
        private val room: Room<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>,
        public val metadata: PlayerMetadata,
    ) {
        public interface Metadata<out ID> {
            public val id: ID
        }
        
        public class Attachment<
            RoomMetadata,
            PlayerID,
            PlayerMetadata : Metadata<PlayerID>,
            WordsProviderID,
            WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
            NoWordsProviderReason,
            ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>
        > internal constructor(
            public val player: Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>,
            private val node: KoneMutableListNode<ConnectionType>,
        ) {
            public suspend fun sever() {
                player.room.stateMachine.moveMaybe {
                    val isNodeActuallyAttached = !node.isDetached
                    
                    if (isNodeActuallyAttached) {
                        node.remove()
                        TransitionOrReason.Success(Transition.UpdatePlayersState())
                    } else {
                        node.element.sendError(Outgoing.Error.AttachmentIsAlreadySevered)
                        TransitionOrReason.Failure(null)
                    }
                }
            }

            public suspend fun fixRoom() {
                val result = player.room.stateMachine.move(Transition.FixRoom())
                when (result) {
                    is MovementResult.NoNextState -> node.element.sendError(result.noNextStateReason)
                    is MovementResult.Success -> {}
                }
            }
            
            public suspend fun updateGameSettings(
                newGlobalRoles: KoneList<GlobalRole>,
                settingsBuilderPatch: GameSettings.Builder.Patch<WordsProviderID>,
                extraSettingsPatch: ExtraSettings.Patch,
            ) {
                val result = player.room.stateMachine.move(
                    Transition.UpdateGameSettings(
                        newGlobalRoles = newGlobalRoles,
                        gameSettingsBuilderPatch = settingsBuilderPatch,
                        extraSettingsPatch = extraSettingsPatch,
                    )
                )
                when (result) {
                    is MovementResult.NoNextState -> node.element.sendError(result.noNextStateReason)
                    is MovementResult.Success -> {}
                }
            }
            
            public suspend fun initializeGame() {
                val result = player.room.stateMachine.move(
                    Transition.InitialiseGame()
                )
                when (result) {
                    is MovementResult.NoNextState -> node.element.sendError(result.noNextStateReason)
                    is MovementResult.Success -> {}
                }
            }
            
            public suspend fun submitWords(words: KoneSet<String>) {
                val result = player.room.stateMachine.moveMaybe { previousState ->
                    TransitionOrReason.Success(
                        Transition.GameInProgressTransition(
                            player,
                            GameStateMachine.Transition.SubmitPlayerWords(
                                playerIndex = (Equality.defaultFor<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>>()) {
                                    when (previousState) {
                                        is State.RoomPlayersGathering -> return@moveMaybe TransitionOrReason.Failure(Outgoing.Error.CannotSubmitPlayerWordsNotDuringPlayersWordsCollection)
                                        is State.GameInitialization -> return@moveMaybe TransitionOrReason.Failure(Outgoing.Error.CannotSubmitPlayerWordsNotDuringPlayersWordsCollection)
                                        is State.GameInProgress -> previousState.playersList.firstIndexOf(player)
                                    }
                                },
                                playerWords = words,
                            )
                        )
                    )
                }
                when (result) {
                    is MovementMaybeResult.NoTransition -> node.element.sendError(result.noTransitionReason)
                    is MovementMaybeResult.NoNextState -> node.element.sendError(result.noNextStateReason)
                    is MovementMaybeResult.Success -> {}
                }
            }
            
            public suspend fun speakerReady() {
                val result = player.room.stateMachine.move(Transition.GameInProgressTransition(player, GameStateMachine.Transition.SpeakerReady))
                when (result) {
                    is MovementResult.NoNextState -> node.element.sendError(result.noNextStateReason)
                    is MovementResult.Success -> {}
                }
            }
            
            public suspend fun listenerReady() {
                val result = player.room.stateMachine.move(Transition.GameInProgressTransition(player, GameStateMachine.Transition.ListenerReady))
                when (result) {
                    is MovementResult.NoNextState -> node.element.sendError(result.noNextStateReason)
                    is MovementResult.Success -> {}
                }
            }
            
            public suspend fun wordExplanationState(state: GameStateMachine.WordExplanation.State) {
                val result = player.room.stateMachine.move(Transition.GameInProgressTransition(player, GameStateMachine.Transition.WordExplanationState(state)))
                when (result) {
                    is MovementResult.NoNextState -> node.element.sendError(result.noNextStateReason)
                    is MovementResult.Success -> {}
                }
            }
            
            public suspend fun updateWordsExplanationResults(newExplanationResults: KoneList<GameStateMachine.WordExplanation>) {
                val result = player.room.stateMachine.move(Transition.GameInProgressTransition(player, GameStateMachine.Transition.UpdateWordsExplanationResults(newExplanationResults)))
                when (result) {
                    is MovementResult.NoNextState -> node.element.sendError(result.noNextStateReason)
                    is MovementResult.Success -> {}
                }
            }
            
            public suspend fun confirmWordsExplanationResults() {
                val result = player.room.stateMachine.move(Transition.GameInProgressTransition(player, GameStateMachine.Transition.ConfirmWordsExplanationResults))
                when (result) {
                    is MovementResult.NoNextState -> node.element.sendError(result.noNextStateReason)
                    is MovementResult.Success -> {}
                }
            }
            
            public suspend fun finishGame() {
                val result = player.room.stateMachine.move(Transition.GameInProgressTransition(player, GameStateMachine.Transition.FinishGame))
                when (result) {
                    is MovementResult.NoNextState -> node.element.sendError(result.noNextStateReason)
                    is MovementResult.Success -> {}
                }
            }
        }
        
        public data class Description<out PlayerMetadata>(
            val metadata: PlayerMetadata,
            val isOnline: Boolean,
            val isHost: Boolean,
        )
        
        internal val connectionsRegistry: KoneMutableNoddedList<ConnectionType> = KoneGCLinkedSizedList()
        public val isOnline: Boolean get() = connectionsRegistry.isNotEmpty()
        public suspend fun sendNewState(state: Outgoing.State<RoomMetadata, PlayerMetadata, WordsProviderDescription>) {
            supervisorScope {
                connectionsRegistry.forEach {
                    launch {
                        it.sendNewState(state)
                    }
                }
            }
        }
        public suspend fun sendError(error: Outgoing.Error<NoWordsProviderReason>) {
            supervisorScope {
                connectionsRegistry.forEach {
                    launch {
                        it.sendError(error)
                    }
                }
            }
        }
    }
    
    public data class Description<out RoomMetadata, out PlayerMetadata>(
        val metadata: RoomMetadata,
        val playersList: KoneList<Player.Description<PlayerMetadata>>,
        val stateType: StateType
    )
    
    public enum class StateType {
        RoomPlayersGathering,
        GameInitialisation,
        PlayersWordsCollection,
        RoundWaiting,
        RoundPreparation,
        RoundExplanation,
        RoundLastGuess,
        RoundEditing,
        GameResults,
    }
    
    public interface Connection<in RoomMetadata, in PlayerMetadata, out WordsProviderID, in WordsProviderDescription: Room.WordsProviderDescription<WordsProviderID>, in NoWordsProviderReason> {
        public suspend fun sendNewState(state: Outgoing.State<RoomMetadata, PlayerMetadata, WordsProviderDescription>)
        public suspend fun sendError(error: Outgoing.Error<NoWordsProviderReason>)
    }

    public interface WordsProviderDescription<out WordsProviderId> {
        public val providerId: WordsProviderId
    }

    public interface WordsProviderRegistry<WordsProviderId, out WordsProviderDescription: Room.WordsProviderDescription<WordsProviderId>, out Reason> : GameStateMachine.WordsProviderRegistry<WordsProviderId, Reason> {
        public suspend fun getWordsProviderDescription(providerId: WordsProviderId): WordsProviderDescriptionOrReason<WordsProviderDescription, Reason>

        public sealed interface WordsProviderDescriptionOrReason<out WordsProviderDescription, out Reason> {
            public data class Success<WordsProviderDescription>(val result: WordsProviderDescription) : WordsProviderDescriptionOrReason<WordsProviderDescription, Nothing>
            public data class Failure<Reason>(val reason: Reason) : WordsProviderDescriptionOrReason<Nothing, Reason>
        }
    }

    public sealed interface WordsSource<out WordsProviderDescription> {
        public data object Players : WordsSource<Nothing>
        public data class Custom<out WordsProviderDescription>(
            public val description: WordsProviderDescription,
        ) : WordsSource<WordsProviderDescription>
    }
    
    public data class GameSettings<out WordsProviderDescription>(
        val preparationTimeSeconds: UInt,
        val explanationTimeSeconds: UInt,
        val finalGuessTimeSeconds: UInt,
        val strictMode: Boolean,
        val gameEndCondition: GameStateMachine.GameEndCondition,
        val wordsSource: WordsSource<WordsProviderDescription>,
    ) {
        public data class Builder<out WordsProviderDescription>(
            val preparationTimeSeconds: UInt,
            val explanationTimeSeconds: UInt,
            val finalGuessTimeSeconds: UInt,
            val strictMode: Boolean,
            val cachedEndConditionWordsNumber: UInt,
            val cachedEndConditionCyclesNumber: UInt,
            val gameEndConditionType: GameStateMachine.GameEndCondition.Type,
            val wordsSource: WordsSource<WordsProviderDescription>,
        ) {
            public data class Patch<out WordsProviderID>(
                val preparationTimeSeconds: UInt?,
                val explanationTimeSeconds: UInt?,
                val finalGuessTimeSeconds: UInt?,
                val strictMode: Boolean?,
                val cachedEndConditionWordsNumber: UInt?,
                val cachedEndConditionCyclesNumber: UInt?,
                val gameEndConditionType: GameStateMachine.GameEndCondition.Type?,
                val wordsSource: WordsSource<WordsProviderID>?,
            )
        }
    }

    public data class ExtraSettings(
        val showWordsStatistic: Boolean,
        val showLeaderboardPermutation: Boolean,
    ) {
        public data class Patch(
            val showWordsStatistic: Boolean?,
            val showLeaderboardPermutation: Boolean?,
        )
    }

    public sealed interface GlobalRole {
        public data object Player : GlobalRole
        public data object Spectator : GlobalRole
    }

    private sealed interface State<
        RoomMetadata,
        PlayerID,
        PlayerMetadata : Player.Metadata<PlayerID>,
        WordsProviderID,
        WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
        NoWordsProviderReason,
        ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>
    > {
        val roomMetadata: RoomMetadata

        data class RoomPlayersGathering<
            RoomMetadata,
            PlayerID,
            PlayerMetadata : Player.Metadata<PlayerID>,
            WordsProviderID,
            WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
            NoWordsProviderReason,
            ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>
        >(
            override val roomMetadata: RoomMetadata,
            val appearedPlayersList: KoneList<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>>,
        ) : State<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>

        data class GameInitialization<
            RoomMetadata,
            PlayerID,
            PlayerMetadata : Player.Metadata<PlayerID>,
            WordsProviderID,
            WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
            NoWordsProviderReason,
            ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>
        >(
            override val roomMetadata: RoomMetadata,
            val playersList: KoneList<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>>,
            val globalRoles: KoneList<GlobalRole>,
            val gameSettingsBuilder: GameSettings.Builder<WordsProviderDescription>,
            val extraSettings: ExtraSettings,
        ) : State<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>

        data class GameInProgress<
            RoomMetadata,
            PlayerID,
            PlayerMetadata : Player.Metadata<PlayerID>,
            WordsProviderID,
            WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
            NoWordsProviderReason,
            ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>
        >(
            override val roomMetadata: RoomMetadata,
            val playersList: KoneList<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>>,
            val globalRoles: KoneList<GlobalRole>,
            val gameSettingsBuilder: GameSettings.Builder<WordsProviderDescription>,
            val extraSettings: ExtraSettings,
            val gameState: GameStateMachine.State.GameInitialised<
                Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>,
                WordsProviderDescription,
            >,
        ) : State<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>
    }

    private sealed interface Transition<
        RoomMetadata,
        PlayerID,
        PlayerMetadata : Player.Metadata<PlayerID>,
        WordsProviderID,
        WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
        NoWordsProviderReason,
        ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>
    > {
        data class AddPlayer<
            RoomMetadata,
            PlayerID,
            PlayerMetadata : Player.Metadata<PlayerID>,
            WordsProviderID,
            WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
            NoWordsProviderReason,
            ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>
        >(
            val newPlayer: Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>
        ) : Transition<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>
        class UpdatePlayersState<
            RoomMetadata,
            PlayerID,
            PlayerMetadata : Player.Metadata<PlayerID>,
            WordsProviderID,
            WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
            NoWordsProviderReason,
            ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>
        > : Transition<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>
        class FixRoom<
            RoomMetadata,
            PlayerID,
            PlayerMetadata : Player.Metadata<PlayerID>,
            WordsProviderID,
            WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
            NoWordsProviderReason,
            ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>
        > : Transition<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>
        data class UpdateGameSettings<
            RoomMetadata,
            PlayerID,
            PlayerMetadata : Player.Metadata<PlayerID>,
            WordsProviderID,
            WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
            NoWordsProviderReason,
            ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>
        >(
            val newGlobalRoles: KoneList<GlobalRole>,
            val gameSettingsBuilderPatch: GameSettings.Builder.Patch<WordsProviderID>,
            val extraSettingsPatch: ExtraSettings.Patch,
        ) : Transition<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>
        class InitialiseGame<
            RoomMetadata,
            PlayerID,
            PlayerMetadata : Player.Metadata<PlayerID>,
            WordsProviderID,
            WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
            NoWordsProviderReason,
            ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>
        > : Transition<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>
        data class GameInProgressTransition<
            RoomMetadata,
            PlayerID,
            PlayerMetadata : Player.Metadata<PlayerID>,
            WordsProviderID,
            WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
            NoWordsProviderReason,
            ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>
        >(
            val playerApplier: Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>,
            val gameStateMachineTransition: GameStateMachine.Transition<
                Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>,
                WordsProviderDescription,
                NoWordsProviderReason,
            >,
        ) : Transition<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>
        data class GameStateMachineInternalTransition<
            RoomMetadata,
            PlayerID,
            PlayerMetadata : Player.Metadata<PlayerID>,
            WordsProviderID,
            WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>,
            NoWordsProviderReason,
            ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>
        >(
            val gameStateMachineTransition: GameStateMachine.Transition<
                Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>,
                WordsProviderDescription,
                NoWordsProviderReason,
            >,
        ) : Transition<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>
    }
    
    public object Outgoing {
        public data class Leaderboard(
            val permutation: KoneUIntArray,
            val scoreExplained: KoneUIntArray,
            val scoreGuessed: KoneUIntArray,
            val scoreSum: KoneUIntArray,
        )

        public sealed interface PlayerDescription<out PlayerMetadata> {
            public val metadata: PlayerMetadata
            public val isOnline: Boolean
            public val isHost: Boolean

            public data class RoomPlayersGathering<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val isOnline: Boolean,
                override val isHost: Boolean,
            ) : PlayerDescription<PlayerMetadata>
            
            public data class GameInitialisation<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val isOnline: Boolean,
                override val isHost: Boolean,
                public val globalRole: GlobalRole,
            ) : PlayerDescription<PlayerMetadata> {
                public sealed interface GlobalRole {
                    public data object Player : GlobalRole
                    public data object Spectator : GlobalRole
                }
            }
            
            public data class PlayersWordsCollection<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val isOnline: Boolean,
                override val isHost: Boolean,
                public val globalRole: GlobalRole,
            ) : PlayerDescription<PlayerMetadata> {
                public sealed interface GlobalRole {
                    public data class Player(
                        public val finishedWordsCollection: Boolean,
                    ) : GlobalRole
                    public data object Spectator : GlobalRole
                }
            }
            
            public sealed interface Round<out PlayerMetadata> : PlayerDescription<PlayerMetadata> {
                public val globalRole: GlobalRole

                @Serializable
                public sealed interface GlobalRole {
                    public data class Player(
                        public val roundRole: RoundRole?,
                    ) : GlobalRole {
                        public enum class RoundRole {
                            Speaker, Listener,
                        }
                    }
                    public data object Spectator : GlobalRole
                }
                
                public data class Waiting<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val globalRole: GlobalRole,
                ) : Round<PlayerMetadata>
                
                public data class Preparation<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val globalRole: GlobalRole,
                ) : Round<PlayerMetadata>
                
                public data class Explanation<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val globalRole: GlobalRole,
                ) : Round<PlayerMetadata>
                
                public data class LastGuess<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val globalRole: GlobalRole,
                ) : Round<PlayerMetadata>
                
                public data class Editing<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val globalRole: GlobalRole,
                ) : Round<PlayerMetadata>
            }
            
            public data class GameResults<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val isOnline: Boolean,
                override val isHost: Boolean,
                public val globalRole: GlobalRole,
            ) : PlayerDescription<PlayerMetadata> {
                public sealed interface GlobalRole {
                    public data object Player : GlobalRole
                    public data object Spectator : GlobalRole
                }
            }
        }
        
        public sealed interface SelfRole<out PlayerMetadata> {
            public val metadata: PlayerMetadata
            public val userIndex: UInt
            public val isHost: Boolean

            public data class RoomPlayersGathering<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val isRoomFixable: Boolean,
            ) : SelfRole<PlayerMetadata>
            
            public data class GameInitialisation<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val globalRole: GlobalRole,
                public val isStartAvailable: Boolean,
                public val areSettingsChangeable: Boolean,
            ) : SelfRole<PlayerMetadata> {
                public sealed interface GlobalRole {
                    public data object Player : GlobalRole
                    public data object Spectator : GlobalRole
                }
            }
            
            public data class PlayersWordsCollection<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val globalRole: GlobalRole,
            ) : SelfRole<PlayerMetadata> {
                public sealed interface GlobalRole {
                    public data class Player(
                        public val finishedWordsCollection: Boolean,
                    ) : GlobalRole
                    public data object Spectator : GlobalRole
                }
            }
            
            public sealed interface Round<out PlayerMetadata> : SelfRole<PlayerMetadata> {
                public data class Waiting<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val globalRole: GlobalRole,
                    public val isGameFinishable: Boolean,
                ) : Round<PlayerMetadata> {
                    public sealed interface GlobalRole {
                        public data class Player(
                            public val roundRole: RoundRole?,
                            public val roundsBeforeSpeaking: UInt,
                            public val roundsBeforeListening: UInt,
                        ) : GlobalRole {
                            @Serializable
                            public enum class RoundRole {
                                Speaker, Listener,
                            }
                        }
                        public data object Spectator : GlobalRole
                    }
                }
                
                public data class Preparation<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val globalRole: GlobalRole,
                ) : Round<PlayerMetadata> {
                    public sealed interface GlobalRole {
                        public data class Player(
                            public val roundRole: RoundRole?,
                            public val roundsBeforeSpeaking: UInt,
                            public val roundsBeforeListening: UInt,
                        ) : GlobalRole {
                            @Serializable
                            public enum class RoundRole {
                                Speaker, Listener,
                            }
                        }
                        public data object Spectator : GlobalRole
                    }
                }
                
                public data class Explanation<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val globalRole: GlobalRole,
                ) : Round<PlayerMetadata> {
                    public sealed interface GlobalRole {
                        public data class Player(
                            public val roundRole: RoundRole?,
                            public val roundsBeforeSpeaking: UInt,
                            public val roundsBeforeListening: UInt,
                        ) : GlobalRole {
                            @Serializable
                            public sealed interface RoundRole {
                                @Serializable
                                public data class Speaker(val currentWord: String) : RoundRole
                                @Serializable
                                public data object Listener : RoundRole
                            }
                        }
                        public data object Spectator : GlobalRole
                    }
                }
                
                public data class LastGuess<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val globalRole: GlobalRole,
                ) : Round<PlayerMetadata> {
                    public sealed interface GlobalRole {
                        public data class Player(
                            public val roundRole: RoundRole?,
                            public val roundsBeforeSpeaking: UInt,
                            public val roundsBeforeListening: UInt,
                        ) : GlobalRole {
                            @Serializable
                            public sealed interface RoundRole {
                                @Serializable
                                public data class Speaker(val currentWord: String) : RoundRole
                                @Serializable
                                public data object Listener : RoundRole
                            }
                        }
                        public data object Spectator : GlobalRole
                    }
                }
                
                public data class Editing<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val globalRole: GlobalRole,
                ) : Round<PlayerMetadata> {
                    public sealed interface GlobalRole {
                        public data class Player(
                            public val roundRole: RoundRole?,
                            public val roundsBeforeSpeaking: UInt,
                            public val roundsBeforeListening: UInt,
                        ) : GlobalRole {
                            @Serializable
                            public sealed interface RoundRole {
                                @Serializable
                                public data class Speaker(
                                    public val wordsToEdit: KoneList<GameStateMachine.WordExplanation>,
                                ) : RoundRole
                                @Serializable
                                public data object Listener : RoundRole
                            }
                        }
                        public data object Spectator : GlobalRole
                    }
                }
            }
            
            public data class GameResults<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val globalRole: GlobalRole,
            ) : SelfRole<PlayerMetadata> {
                public sealed interface GlobalRole {
                    public data object Player: GlobalRole
                    public data object Spectator : GlobalRole
                }
            }
        }
        
        public sealed interface State<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription> {
            public val roomMetadata: RoomMetadata
            public val selfRole: SelfRole<PlayerMetadata>
            public val playersList: KoneList<PlayerDescription<PlayerMetadata>>

            public data class RoomPlayersGathering<out RoomMetadata, out PlayerMetadata>(
                override val roomMetadata: RoomMetadata,
                override val selfRole: SelfRole.RoomPlayersGathering<PlayerMetadata>,
                override val playersList: KoneList<PlayerDescription.RoomPlayersGathering<PlayerMetadata>>,
            ) : State<RoomMetadata, PlayerMetadata, Nothing>
            
            public data class GameInitialisation<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                override val roomMetadata: RoomMetadata,
                override val selfRole: SelfRole.GameInitialisation<PlayerMetadata>,
                override val playersList: KoneList<PlayerDescription.GameInitialisation<PlayerMetadata>>,
                public val settingsBuilder: GameSettings.Builder<WordsProviderDescription>,
                public val extraSettings: ExtraSettings,
            ) : State<RoomMetadata, PlayerMetadata, WordsProviderDescription>
            
            public data class PlayersWordsCollection<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                override val roomMetadata: RoomMetadata,
                override val selfRole: SelfRole.PlayersWordsCollection<PlayerMetadata>,
                override val playersList: KoneList<PlayerDescription.PlayersWordsCollection<PlayerMetadata>>,
                public val settings: GameSettings<WordsProviderDescription>,
                public val extraSettings: ExtraSettings,
            ) : State<RoomMetadata, PlayerMetadata, WordsProviderDescription>
            
            public sealed interface Round<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription> : State<RoomMetadata, PlayerMetadata, WordsProviderDescription> {
                override val selfRole: SelfRole.Round<PlayerMetadata>
                override val playersList: KoneList<PlayerDescription.Round<PlayerMetadata>>
                public val settings: GameSettings<WordsProviderDescription>
                public val extraSettings: ExtraSettings
                public val initialWordsNumber: UInt
                public val roundNumber: UInt
                public val cycleNumber: UInt
                public val speakerIndex: UInt
                public val listenerIndex: UInt
                public val nextSpeakerIndex: UInt
                public val nextListenerIndex: UInt
                public val restWordsNumber: UInt
                public val wordsInProgressNumber: UInt
                public val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>?
                public val leaderboard: Leaderboard?
                
                public data class Waiting<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                    override val roomMetadata: RoomMetadata,
                    override val selfRole: SelfRole.Round.Waiting<PlayerMetadata>,
                    override val playersList: KoneList<PlayerDescription.Round.Waiting<PlayerMetadata>>,
                    override val settings: GameSettings<WordsProviderDescription>,
                    override val extraSettings: ExtraSettings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>?,
                    public val speakerReady: Boolean,
                    public val listenerReady: Boolean,
                    override val leaderboard: Leaderboard?,
                ) : Round<RoomMetadata, PlayerMetadata, WordsProviderDescription>
                
                public data class Preparation<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                    override val roomMetadata: RoomMetadata,
                    override val selfRole: SelfRole.Round.Preparation<PlayerMetadata>,
                    override val playersList: KoneList<PlayerDescription.Round.Preparation<PlayerMetadata>>,
                    override val settings: GameSettings<WordsProviderDescription>,
                    override val extraSettings: ExtraSettings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>?,
                    public val millisecondsLeft: UInt,
                    override val leaderboard: Leaderboard?,
                ) : Round<RoomMetadata, PlayerMetadata, WordsProviderDescription>
                
                public data class Explanation<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                    override val roomMetadata: RoomMetadata,
                    override val selfRole: SelfRole.Round.Explanation<PlayerMetadata>,
                    override val playersList: KoneList<PlayerDescription.Round.Explanation<PlayerMetadata>>,
                    override val settings: GameSettings<WordsProviderDescription>,
                    override val extraSettings: ExtraSettings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>?,
                    public val millisecondsLeft: UInt,
                    override val leaderboard: Leaderboard?,
                ) : Round<RoomMetadata, PlayerMetadata, WordsProviderDescription>
                
                public data class LastGuess<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                    override val roomMetadata: RoomMetadata,
                    override val selfRole: SelfRole.Round.LastGuess<PlayerMetadata>,
                    override val playersList: KoneList<PlayerDescription.Round.LastGuess<PlayerMetadata>>,
                    override val settings: GameSettings<WordsProviderDescription>,
                    override val extraSettings: ExtraSettings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>?,
                    public val millisecondsLeft: UInt,
                    override val leaderboard: Leaderboard?,
                ) : Round<RoomMetadata, PlayerMetadata, WordsProviderDescription>
                
                public data class Editing<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                    override val roomMetadata: RoomMetadata,
                    override val selfRole: SelfRole.Round.Editing<PlayerMetadata>,
                    override val playersList: KoneList<PlayerDescription.Round.Editing<PlayerMetadata>>,
                    override val settings: GameSettings<WordsProviderDescription>,
                    override val extraSettings: ExtraSettings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>?,
                    override val leaderboard: Leaderboard?,
                ) : Round<RoomMetadata, PlayerMetadata, WordsProviderDescription>
            }
            
            public data class GameResults<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                override val roomMetadata: RoomMetadata,
                override val selfRole: SelfRole.GameResults<PlayerMetadata>,
                override val playersList: KoneList<PlayerDescription.GameResults<PlayerMetadata>>,
                val settings: GameSettings<WordsProviderDescription>,
                val extraSettings: ExtraSettings,
                val leaderboard: Leaderboard,
                val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>,
            ) : State<RoomMetadata, PlayerMetadata, WordsProviderDescription>
        }
        
        public sealed interface Error<out NoWordsProviderReason> {
            public data object AttachmentIsDenied : Error<Nothing>
            public data object AttachmentIsAlreadySevered : Error<Nothing>
            public data object RoomIsAlreadyFixed : Error<Nothing>
            public data object NoGameSettingsToChange : Error<Nothing>
            public data object UnableToApplyGameStateMachineTransition : Error<Nothing>
            public data object NotHostChangingGameSettings : Error<Nothing>
            public data object IncorrectNumberOfGlobalRoles : Error<Nothing>
            public data object CannotInitializeGameNotDuringGameInitialisation : Error<Nothing>
            public data object CannotUpdateGameSettingsAfterInitialization : Error<Nothing>
            public data object NotEnoughPlayersForInitialization : Error<Nothing>
            public data class NoWordsProvider<out NoWordsProviderReason>(val reason: NoWordsProviderReason) : Error<NoWordsProviderReason>
            public data object CannotInitializeGameAfterInitialization : Error<Nothing>
            public data object PlayerAlreadySubmittedWords : Error<Nothing>
            public data object CannotSubmitPlayerWordsNotDuringPlayersWordsCollection : Error<Nothing>
            public data object CannotSetSpeakerReadinessNotDuringRoundWaiting : Error<Nothing>
            public data object NotSpeakerSettingSpeakerReadiness : Error<Nothing>
            public data object CannotSetListenerReadinessNotDuringRoundWaiting : Error<Nothing>
            public data object NotListenerSettingListenerReadiness : Error<Nothing>
            public data object ForbiddenSpeakerAndListenerReadyTransition : Error<Nothing>
            public data object ForbiddenUpdateRoundInfoTransition : Error<Nothing>
            public data object CannotUpdateRoundInfoNotDuringTheRound : Error<Nothing>
            public data object CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess : Error<Nothing>
            public data object NotSpeakerSubmittingWordExplanationResult : Error<Nothing>
            public data object CannotUpdateWordExplanationResultsNotDuringRoundEditing : Error<Nothing>
            public data object NotSpeakerUpdatingWordExplanationResults : Error<Nothing>
            public data object CannotUpdateWordExplanationResultsWithOtherWordsSet : Error<Nothing>
            public data object CannotConfirmWordExplanationResultsNotDuringRoundEditing : Error<Nothing>
            public data object NotSpeakerConfirmingWordExplanationResults : Error<Nothing>
            public data object CannotFinishGameNotDuringRoundWaiting : Error<Nothing>
            public data object NotHostFinishingGame : Error<Nothing>
            
            public companion object {
                internal fun <NoWordsProviderReason> fromGameStateMachineNoNextStateReason(reason: GameStateMachine.NoNextStateReason<NoWordsProviderReason>): Error<NoWordsProviderReason> =
                    when (reason) {
                        GameStateMachine.NoNextStateReason.CannotUpdateGameSettingsAfterInitialization -> CannotUpdateGameSettingsAfterInitialization
                        GameStateMachine.NoNextStateReason.NotEnoughPlayersForInitialization -> NotEnoughPlayersForInitialization
                        is GameStateMachine.NoNextStateReason.NoWordsProvider<NoWordsProviderReason> -> NoWordsProvider(reason.reason)
                        GameStateMachine.NoNextStateReason.CannotInitializeGameAfterInitialization -> CannotInitializeGameAfterInitialization
                        GameStateMachine.NoNextStateReason.PlayerAlreadySubmittedWords -> PlayerAlreadySubmittedWords
                        GameStateMachine.NoNextStateReason.CannotSubmitPlayerWordsNotDuringPlayersWordsCollection -> CannotSubmitPlayerWordsNotDuringPlayersWordsCollection
                        GameStateMachine.NoNextStateReason.CannotSetSpeakerReadinessNotDuringRoundWaiting -> CannotSetSpeakerReadinessNotDuringRoundWaiting
                        GameStateMachine.NoNextStateReason.CannotSetListenerReadinessNotDuringRoundWaiting -> CannotSetListenerReadinessNotDuringRoundWaiting
                        GameStateMachine.NoNextStateReason.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting -> ForbiddenSpeakerAndListenerReadyTransition
                        GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound -> CannotUpdateRoundInfoNotDuringTheRound
                        GameStateMachine.NoNextStateReason.CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess -> CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess
                        GameStateMachine.NoNextStateReason.CannotUpdateWordExplanationResultsNotDuringRoundEditing -> CannotUpdateWordExplanationResultsNotDuringRoundEditing
                        GameStateMachine.NoNextStateReason.CannotUpdateWordExplanationResultsWithOtherWordsSet -> CannotUpdateWordExplanationResultsWithOtherWordsSet
                        GameStateMachine.NoNextStateReason.CannotConfirmWordExplanationResultsNotDuringRoundEditing -> CannotConfirmWordExplanationResultsNotDuringRoundEditing
                        GameStateMachine.NoNextStateReason.CannotFinishGameNotDuringRoundWaiting -> CannotFinishGameNotDuringRoundWaiting
                    }
            }
        }
    }
    
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private val random: Random = Random
    private val timerDelayDuration: Duration = 90.milliseconds
    
    private val wordsStatisticStateOrder: KoneList<GameStateMachine.WordStatistic.State> =
        KoneList.of(
            GameStateMachine.WordStatistic.State.Explained,
            GameStateMachine.WordStatistic.State.InProgress,
            GameStateMachine.WordStatistic.State.Mistake,
        )
    private fun KoneMap<String, GameStateMachine.WordStatistic>.toOutgoingAPI(): KoneList<GameStateMachine.WordStatistic.AndWord> =
        nodesView
            .map {
                GameStateMachine.WordStatistic.AndWord(
                    word = it.key,
                    spentTime = it.value.spentTime,
                    state = it.value.state,
                )
            }.sortedWith { left, right -> // TODO: Rewrite with comparator utilities
                val wordsComparisonResult = context(UInt.order(), Equality.defaultFor<GameStateMachine.WordStatistic.State>()) {
                    wordsStatisticStateOrder.firstIndexOf(left.state) compareWith wordsStatisticStateOrder.firstIndexOf(right.state)
                }
                if (wordsComparisonResult != ComparisonResult.Equal) return@sortedWith wordsComparisonResult
                return@sortedWith (Order.defaultFor<Duration>()) { right.spentTime compareWith left.spentTime }
            }

    private val stateMachine = AsynchronousAutomaton<
        State<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>,
        Transition<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>,
        Outgoing.Error<NoWordsProviderReason>,
    >(
        mutex = Mutex(),
        initialState = State.RoomPlayersGathering(
            roomMetadata = metadata,
            appearedPlayersList = initialPlayersList.map { Player(this, it) },
        ),
        checkTransition = checkTransition@ { state, transition ->
            when (transition) {
                is Transition.AddPlayer -> when (state) {
                    is State.RoomPlayersGathering ->
                        CheckResult.Success(
                            State.RoomPlayersGathering(
                                roomMetadata = state.roomMetadata,
                                appearedPlayersList = KoneList.build {
                                    addAllFrom(state.appearedPlayersList)
                                    +transition.newPlayer
                                }
                            )
                        )
                    is State.GameInitialization -> CheckResult.Failure(Outgoing.Error.AttachmentIsDenied)
                    is State.GameInProgress -> {
                        CheckResult.Success(
                            State.GameInProgress(
                                roomMetadata = state.roomMetadata,
                                playersList = KoneList.build {
                                    addAllFrom(state.playersList)
                                    +transition.newPlayer
                                },
                                globalRoles = KoneList.build {
                                    addAllFrom(state.globalRoles)
                                    +GlobalRole.Spectator
                                },
                                gameSettingsBuilder = state.gameSettingsBuilder,
                                extraSettings = state.extraSettings,
                                gameState = state.gameState,
                            )
                        )
                    }
                }
                is Transition.UpdatePlayersState -> CheckResult.Success(state)
                is Transition.FixRoom -> when (state) {
                    is State.RoomPlayersGathering -> {
                        val onlinePlayersIndices = state.appearedPlayersList.withIndex().filter { it.value.isOnline }
                        CheckResult.Success(
                            State.GameInitialization(
                                roomMetadata = state.roomMetadata,
                                playersList = onlinePlayersIndices.map { it.value },
                                globalRoles = onlinePlayersIndices.map { initialGlobalRoles.getOrNull(it.index) ?: GlobalRole.Player },
                                gameSettingsBuilder = initialSettingsBuilder,
                                extraSettings = initialExtraSettings,
                            )
                        )
                    }
                    is State.GameInitialization -> CheckResult.Failure(Outgoing.Error.RoomIsAlreadyFixed)
                    is State.GameInProgress -> CheckResult.Failure(Outgoing.Error.RoomIsAlreadyFixed)
                }
                is Transition.UpdateGameSettings -> when (state) {
                    is State.RoomPlayersGathering -> CheckResult.Failure(Outgoing.Error.NoGameSettingsToChange)
                    is State.GameInitialization ->
                        CheckResult.Success(
                            State.GameInitialization(
                                roomMetadata = state.roomMetadata,
                                playersList = state.playersList,
                                globalRoles = transition.newGlobalRoles.also {
                                    if (it.size != state.playersList.size) return@checkTransition CheckResult.Failure(Outgoing.Error.IncorrectNumberOfGlobalRoles)
                                },
                                gameSettingsBuilder = GameSettings.Builder(
                                    preparationTimeSeconds = transition.gameSettingsBuilderPatch.preparationTimeSeconds ?: state.gameSettingsBuilder.preparationTimeSeconds,
                                    explanationTimeSeconds = transition.gameSettingsBuilderPatch.explanationTimeSeconds ?: state.gameSettingsBuilder.explanationTimeSeconds,
                                    finalGuessTimeSeconds = transition.gameSettingsBuilderPatch.finalGuessTimeSeconds ?: state.gameSettingsBuilder.finalGuessTimeSeconds,
                                    strictMode = transition.gameSettingsBuilderPatch.strictMode ?: state.gameSettingsBuilder.strictMode,
                                    cachedEndConditionWordsNumber = transition.gameSettingsBuilderPatch.cachedEndConditionWordsNumber ?: state.gameSettingsBuilder.cachedEndConditionWordsNumber,
                                    cachedEndConditionCyclesNumber = transition.gameSettingsBuilderPatch.cachedEndConditionCyclesNumber ?: state.gameSettingsBuilder.cachedEndConditionCyclesNumber,
                                    gameEndConditionType = transition.gameSettingsBuilderPatch.gameEndConditionType ?: state.gameSettingsBuilder.gameEndConditionType,
                                    wordsSource = when (val wordsSource = transition.gameSettingsBuilderPatch.wordsSource) {
                                        null -> state.gameSettingsBuilder.wordsSource
                                        WordsSource.Players -> WordsSource.Players
                                        is WordsSource.Custom -> when (val wordsProviderId = wordsProviderRegistry.getWordsProviderDescription(wordsSource.description)) {
                                            is WordsProviderRegistry.WordsProviderDescriptionOrReason.Failure -> return@checkTransition CheckResult.Failure(Outgoing.Error.NoWordsProvider(wordsProviderId.reason))
                                            is WordsProviderRegistry.WordsProviderDescriptionOrReason.Success -> WordsSource.Custom(wordsProviderId.result)
                                        }
                                    },
                                ),
                                extraSettings = ExtraSettings(
                                    showWordsStatistic = transition.extraSettingsPatch.showWordsStatistic ?: state.extraSettings.showWordsStatistic,
                                    showLeaderboardPermutation = transition.extraSettingsPatch.showLeaderboardPermutation ?: state.extraSettings.showLeaderboardPermutation,
                                ),
                            )
                        )
                    is State.GameInProgress -> CheckResult.Failure(Outgoing.Error.CannotUpdateGameSettingsAfterInitialization)
                }
                is Transition.InitialiseGame -> when (state) {
                    is State.RoomPlayersGathering -> CheckResult.Failure(Outgoing.Error.CannotInitializeGameNotDuringGameInitialisation)
                    is State.GameInitialization -> scope {
                        val playersList = state.playersList
                        val roles = state.globalRoles
                        val settingsBuilder = state.gameSettingsBuilder

                        if (roles.count { it is GlobalRole.Player } < 2u) return@scope CheckResult.Failure(Outgoing.Error.NotEnoughPlayersForInitialization)

                        when (val wordsSource = settingsBuilder.wordsSource) {
                            WordsSource.Players ->
                                CheckResult.Success(
                                    State.GameInProgress(
                                        roomMetadata = state.roomMetadata,
                                        playersList = playersList,
                                        globalRoles = roles,
                                        gameSettingsBuilder = settingsBuilder,
                                        extraSettings = state.extraSettings,
                                        gameState = GameStateMachine.State.GameInitialised.PlayersWordsCollection(
                                            playersList = playersList.withIndex().filter { roles[it.index] == GlobalRole.Player }.map { it.value },
                                            settings = GameStateMachine.GameSettings(
                                                preparationTimeSeconds = settingsBuilder.preparationTimeSeconds,
                                                explanationTimeSeconds = settingsBuilder.explanationTimeSeconds,
                                                finalGuessTimeSeconds = settingsBuilder.finalGuessTimeSeconds,
                                                strictMode = settingsBuilder.strictMode,
                                                gameEndCondition = when (settingsBuilder.gameEndConditionType) {
                                                    GameStateMachine.GameEndCondition.Type.Words -> GameStateMachine.GameEndCondition.Words(settingsBuilder.cachedEndConditionWordsNumber)
                                                    GameStateMachine.GameEndCondition.Type.Cycles -> GameStateMachine.GameEndCondition.Cycles(settingsBuilder.cachedEndConditionCyclesNumber)
                                                },
                                                wordsSource = when (val wordsSource = settingsBuilder.wordsSource) {
                                                    WordsSource.Players -> GameStateMachine.WordsSource.Players
                                                    is WordsSource.Custom -> GameStateMachine.WordsSource.Custom(wordsSource.description)
                                                },
                                            ),
                                            playersWords = KoneList.generate(playersList.size) { null }
                                        ),
                                    )
                                )
                            is WordsSource.Custom -> {
                                val wordsProviderOrReason = wordsProviderRegistry.getWordsProvider(wordsSource.description.providerId)

                                when (wordsProviderOrReason) {
                                    is GameStateMachine.WordsProviderRegistry.WordsProviderOrReason.Failure ->
                                        CheckResult.Failure(Outgoing.Error.NoWordsProvider(wordsProviderOrReason.reason))
                                    is GameStateMachine.WordsProviderRegistry.WordsProviderOrReason.Success -> {
                                        val restWords = when (settingsBuilder.gameEndConditionType) {
                                            GameStateMachine.GameEndCondition.Type.Words -> wordsProviderOrReason.result.randomWords(settingsBuilder.cachedEndConditionWordsNumber)
                                            GameStateMachine.GameEndCondition.Type.Cycles -> wordsProviderOrReason.result.allWords()
                                        }
                                        val nextPair = nextScheduledPairFor(playersList.size, ScheduledPair(0u, 1u))
                                        val schedule = scheduleFor(playersList.size, ScheduledPair(0u, 1u))
                                        CheckResult.Success(
                                            State.GameInProgress(
                                                roomMetadata = state.roomMetadata,
                                                playersList = playersList,
                                                globalRoles = roles,
                                                gameSettingsBuilder = settingsBuilder,
                                                extraSettings = state.extraSettings,
                                                gameState = GameStateMachine.State.GameInitialised.Round.RoundWaiting(
                                                    playersList = playersList.withIndex().filter { roles[it.index] == GlobalRole.Player }.map { it.value },
                                                    settings = GameStateMachine.GameSettings(
                                                        preparationTimeSeconds = settingsBuilder.preparationTimeSeconds,
                                                        explanationTimeSeconds = settingsBuilder.explanationTimeSeconds,
                                                        finalGuessTimeSeconds = settingsBuilder.finalGuessTimeSeconds,
                                                        strictMode = settingsBuilder.strictMode,
                                                        gameEndCondition = when (settingsBuilder.gameEndConditionType) {
                                                            GameStateMachine.GameEndCondition.Type.Words -> GameStateMachine.GameEndCondition.Words(settingsBuilder.cachedEndConditionWordsNumber)
                                                            GameStateMachine.GameEndCondition.Type.Cycles -> GameStateMachine.GameEndCondition.Cycles(settingsBuilder.cachedEndConditionCyclesNumber)
                                                        },
                                                        wordsSource = when (val wordsSource = settingsBuilder.wordsSource) {
                                                            WordsSource.Players -> GameStateMachine.WordsSource.Players
                                                            is WordsSource.Custom -> GameStateMachine.WordsSource.Custom(wordsSource.description)
                                                        },
                                                    ),
                                                    initialWordsNumber = restWords.size,
                                                    roundNumber = 0u,
                                                    cycleNumber = 0u,
                                                    speakerIndex = 0u,
                                                    listenerIndex = 1u,
                                                    nextSpeakerIndex = nextPair.speakerIndex,
                                                    nextListenerIndex = nextPair.listenerIndex,
                                                    playersRoundsBeforeSpeaking = schedule.playersRoundsBeforeSpeaking,
                                                    playersRoundsBeforeListening = schedule.playersRoundsBeforeListening,
                                                    restWords = restWords,
                                                    explanationScores = KoneList.generate(playersList.size) { 0u },
                                                    guessingScores = KoneList.generate(playersList.size) { 0u },
                                                    wordsStatistic = KoneMap.of(),
                                                    speakerReady = false,
                                                    listenerReady = false,
                                                ),
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is State.GameInProgress -> CheckResult.Failure(Outgoing.Error.CannotInitializeGameNotDuringGameInitialisation)
                }
                is Transition.GameInProgressTransition -> when (state) {
                    is State.RoomPlayersGathering,
                    is State.GameInitialization,
                        -> CheckResult.Failure(Outgoing.Error.UnableToApplyGameStateMachineTransition)
                    is State.GameInProgress -> {
                        when (val gameTransition = transition.gameStateMachineTransition) {
                            is GameStateMachine.Transition.UpdateGameSettings -> {
                                if (state.playersList.firstThat { it.isOnline } != transition.playerApplier) {
                                    return@checkTransition CheckResult.Failure(Outgoing.Error.NotHostChangingGameSettings)
                                }
                            }
                            is GameStateMachine.Transition.InitialiseGame -> {
                                if (state.playersList.firstThat { it.isOnline } != transition.playerApplier) {
                                    return@checkTransition CheckResult.Failure(Outgoing.Error.NotHostChangingGameSettings)
                                }
                            }
                            is GameStateMachine.Transition.SubmitPlayerWords -> {}
                            GameStateMachine.Transition.SpeakerReady -> {
                                when (val gameState = state.gameState) {
                                    is GameStateMachine.State.GameInitialised.Round.RoundWaiting ->
                                        if (gameState.speaker != transition.playerApplier) {
                                            return@checkTransition CheckResult.Failure(Outgoing.Error.NotSpeakerSettingSpeakerReadiness)
                                        }
                                    is GameStateMachine.State.GameInitialised.PlayersWordsCollection,
                                    is GameStateMachine.State.GameInitialised.Round.RoundPreparation,
                                    is GameStateMachine.State.GameInitialised.Round.RoundExplanation,
                                    is GameStateMachine.State.GameInitialised.Round.RoundLastGuess,
                                    is GameStateMachine.State.GameInitialised.Round.RoundEditing,
                                    is GameStateMachine.State.GameInitialised.GameResults,
                                        -> {}
                                }
                            }
                            GameStateMachine.Transition.ListenerReady -> {
                                when (val gameState = state.gameState) {
                                    is GameStateMachine.State.GameInitialised.Round.RoundWaiting ->
                                        if (gameState.listener != transition.playerApplier) {
                                            return@checkTransition CheckResult.Failure(Outgoing.Error.NotListenerSettingListenerReadiness)
                                        }
                                    is GameStateMachine.State.GameInitialised.PlayersWordsCollection,
                                    is GameStateMachine.State.GameInitialised.Round.RoundPreparation,
                                    is GameStateMachine.State.GameInitialised.Round.RoundExplanation,
                                    is GameStateMachine.State.GameInitialised.Round.RoundLastGuess,
                                    is GameStateMachine.State.GameInitialised.Round.RoundEditing,
                                    is GameStateMachine.State.GameInitialised.GameResults,
                                        -> {}
                                }
                            }
                            GameStateMachine.Transition.SpeakerAndListenerReady -> return@checkTransition CheckResult.Failure(Outgoing.Error.ForbiddenSpeakerAndListenerReadyTransition)
                            is GameStateMachine.Transition.UpdateRoundInfo -> return@checkTransition CheckResult.Failure(Outgoing.Error.ForbiddenUpdateRoundInfoTransition)
                            is GameStateMachine.Transition.WordExplanationState -> {
                                when (val previousGameState = state.gameState) {
                                    is GameStateMachine.State.GameInitialised.Round.RoundExplanation ->
                                        if (previousGameState.speaker != transition.playerApplier) {
                                            return@checkTransition CheckResult.Failure(Outgoing.Error.NotSpeakerSubmittingWordExplanationResult)
                                        }
                                    is GameStateMachine.State.GameInitialised.Round.RoundLastGuess ->
                                        if (previousGameState.speaker != transition.playerApplier) {
                                            return@checkTransition CheckResult.Failure(Outgoing.Error.NotSpeakerSubmittingWordExplanationResult)
                                        }
                                    is GameStateMachine.State.GameInitialised.PlayersWordsCollection,
                                    is GameStateMachine.State.GameInitialised.Round.RoundWaiting,
                                    is GameStateMachine.State.GameInitialised.Round.RoundPreparation,
                                    is GameStateMachine.State.GameInitialised.Round.RoundEditing,
                                    is GameStateMachine.State.GameInitialised.GameResults,
                                        -> {}
                                }
                            }
                            is GameStateMachine.Transition.UpdateWordsExplanationResults -> {
                                when (val previousState = state.gameState) {
                                    is GameStateMachine.State.GameInitialised.Round.RoundEditing ->
                                        if (previousState.speaker != transition.playerApplier) {
                                            return@checkTransition CheckResult.Failure(Outgoing.Error.NotSpeakerUpdatingWordExplanationResults)
                                        }
                                    is GameStateMachine.State.GameInitialised.PlayersWordsCollection,
                                    is GameStateMachine.State.GameInitialised.Round.RoundWaiting,
                                    is GameStateMachine.State.GameInitialised.Round.RoundPreparation,
                                    is GameStateMachine.State.GameInitialised.Round.RoundExplanation,
                                    is GameStateMachine.State.GameInitialised.Round.RoundLastGuess,
                                    is GameStateMachine.State.GameInitialised.GameResults,
                                        -> {}
                                }
                            }
                            GameStateMachine.Transition.ConfirmWordsExplanationResults -> {
                                when (val previousState = state.gameState) {
                                    is GameStateMachine.State.GameInitialised.Round.RoundEditing ->
                                        if (previousState.speaker != transition.playerApplier) {
                                            return@checkTransition CheckResult.Failure(Outgoing.Error.NotSpeakerConfirmingWordExplanationResults)
                                        }
                                    is GameStateMachine.State.GameInitialised.PlayersWordsCollection,
                                    is GameStateMachine.State.GameInitialised.Round.RoundWaiting,
                                    is GameStateMachine.State.GameInitialised.Round.RoundPreparation,
                                    is GameStateMachine.State.GameInitialised.Round.RoundExplanation,
                                    is GameStateMachine.State.GameInitialised.Round.RoundLastGuess,
                                    is GameStateMachine.State.GameInitialised.GameResults,
                                        -> {}
                                }
                            }
                            GameStateMachine.Transition.FinishGame -> {
                                if (state.playersList.firstThat { it.isOnline } != transition.playerApplier) {
                                    return@checkTransition CheckResult.Failure(Outgoing.Error.NotHostFinishingGame)
                                }
                            }
                        }
                        when (
                            val gameMoveCheckResult = checkGameStateMachineTransitionForGameInitialisedState(
                                coroutineScope = coroutineScope,
                                random = random,
                                timerDelayDuration = timerDelayDuration,
                                moveState = { this.move(Transition.GameStateMachineInternalTransition(it)) },
                                previousState = state.gameState,
                                transition = transition.gameStateMachineTransition,
                            )
                        ) {
                            is CheckResult.Failure -> CheckResult.Failure(Outgoing.Error.fromGameStateMachineNoNextStateReason(gameMoveCheckResult.reason))
                            is CheckResult.Success -> CheckResult.Success(
                                State.GameInProgress(
                                    roomMetadata = state.roomMetadata,
                                    playersList = state.playersList,
                                    globalRoles = state.globalRoles,
                                    gameSettingsBuilder = state.gameSettingsBuilder,
                                    extraSettings = state.extraSettings,
                                    gameState = gameMoveCheckResult.nextState,
                                )
                            )
                        }
                    }
                }
                is Transition.GameStateMachineInternalTransition -> when (state) {
                    is State.RoomPlayersGathering,
                    is State.GameInitialization,
                        -> CheckResult.Failure(Outgoing.Error.UnableToApplyGameStateMachineTransition)
                    is State.GameInProgress -> {
                        when (
                            val gameMoveCheckResult = checkGameStateMachineTransitionForGameInitialisedState(
                                coroutineScope = coroutineScope,
                                random = random,
                                timerDelayDuration = timerDelayDuration,
                                moveState = { this.move(Transition.GameStateMachineInternalTransition(it)) },
                                previousState = state.gameState,
                                transition = transition.gameStateMachineTransition,
                            )
                        ) {
                            is CheckResult.Failure -> CheckResult.Failure(Outgoing.Error.fromGameStateMachineNoNextStateReason(gameMoveCheckResult.reason))
                            is CheckResult.Success -> CheckResult.Success(
                                State.GameInProgress(
                                    roomMetadata = state.roomMetadata,
                                    playersList = state.playersList,
                                    globalRoles = state.globalRoles,
                                    gameSettingsBuilder = state.gameSettingsBuilder,
                                    extraSettings = state.extraSettings,
                                    gameState = gameMoveCheckResult.nextState,
                                )
                            )
                        }
                    }
                }
            }
        },
    ) { _, _, nextState ->
        when (nextState) {
            is State.RoomPlayersGathering -> {
                val onlinePlayersList = nextState.appearedPlayersList.filter { it.isOnline }
                val hostIndex = 0u
                val playersList = onlinePlayersList.mapIndexed { index, player ->
                    Outgoing.PlayerDescription.RoomPlayersGathering(
                        metadata = player.metadata,
                        isOnline = player.isOnline,
                        isHost = index == 0u,
                    )
                }
                val isRoomFixable = onlinePlayersList.size >= 2u
                onlinePlayersList.forEachIndexed { index, player ->
                    player.sendNewState(
                        Outgoing.State.RoomPlayersGathering(
                            roomMetadata = nextState.roomMetadata,
                            selfRole = Outgoing.SelfRole.RoomPlayersGathering(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                                isRoomFixable = isRoomFixable && index == hostIndex,
                            ),
                            playersList = playersList,
                        )
                    )
                }
            }
            is State.GameInitialization -> {
                val hostIndex = nextState.playersList.firstIndexThat { _, player -> player.isOnline }
                val isStartAvailable = nextState.globalRoles.count { it == GlobalRole.Player } >= 2u
                val playersList = nextState.playersList.mapIndexed { index, player ->
                    Outgoing.PlayerDescription.GameInitialisation(
                        metadata = player.metadata,
                        isOnline = player.isOnline,
                        isHost = index == hostIndex,
                        globalRole = when (val globalRole = nextState.globalRoles[index]) {
                            GlobalRole.Player -> Outgoing.PlayerDescription.GameInitialisation.GlobalRole.Player
                            GlobalRole.Spectator -> Outgoing.PlayerDescription.GameInitialisation.GlobalRole.Spectator
                        },
                    )
                }
                nextState.playersList.forEachIndexed { index, player ->
                    player.sendNewState(
                        Outgoing.State.GameInitialisation(
                            roomMetadata = metadata,
                            selfRole = Outgoing.SelfRole.GameInitialisation(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                                globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                    GlobalRole.Player -> Outgoing.SelfRole.GameInitialisation.GlobalRole.Player
                                    GlobalRole.Spectator -> Outgoing.SelfRole.GameInitialisation.GlobalRole.Spectator
                                },
                                isStartAvailable = isStartAvailable && index == hostIndex,
                                areSettingsChangeable = index == hostIndex,
                            ),
                            playersList = playersList,
                            settingsBuilder = nextState.gameSettingsBuilder,
                            extraSettings = nextState.extraSettings,
                        )
                    )
                }
            }
            is State.GameInProgress -> when (val nextGameState = nextState.gameState) {
                is GameStateMachine.State.GameInitialised.PlayersWordsCollection -> {
                    val hostIndex = nextState.playersList.firstIndexThat { _, player -> player.isOnline }
                    val playersList = scope {
                        var playingPlayerIndex = 0u
                        nextState.playersList.mapIndexed { index, player ->
                            Outgoing.PlayerDescription.PlayersWordsCollection(
                                metadata = player.metadata,
                                isOnline = player.isOnline,
                                isHost = index == hostIndex,
                                globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                    GlobalRole.Player -> Outgoing.PlayerDescription.PlayersWordsCollection.GlobalRole.Player(nextGameState.playersWords[playingPlayerIndex] != null).also { playingPlayerIndex++ }
                                    GlobalRole.Spectator -> Outgoing.PlayerDescription.PlayersWordsCollection.GlobalRole.Spectator
                                },
                            )
                        }
                    }
                    val settings = GameSettings(
                        preparationTimeSeconds = nextGameState.settings.preparationTimeSeconds,
                        explanationTimeSeconds = nextGameState.settings.explanationTimeSeconds,
                        finalGuessTimeSeconds = nextGameState.settings.finalGuessTimeSeconds,
                        strictMode = nextGameState.settings.strictMode,
                        gameEndCondition = nextGameState.settings.gameEndCondition,
                        wordsSource = when (val wordsSource = nextGameState.settings.wordsSource) {
                            GameStateMachine.WordsSource.Players -> WordsSource.Players
                            is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                        },
                    )
                    var playingPlayerIndex = 0u
                    nextState.playersList.forEachIndexed { index, player ->
                        player.sendNewState(
                            Outgoing.State.PlayersWordsCollection(
                                roomMetadata = metadata,
                                selfRole = Outgoing.SelfRole.PlayersWordsCollection(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isHost = index == hostIndex,
                                    globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                        GlobalRole.Player -> Outgoing.SelfRole.PlayersWordsCollection.GlobalRole.Player(nextGameState.playersWords[playingPlayerIndex] != null).also { playingPlayerIndex++ }
                                        GlobalRole.Spectator -> Outgoing.SelfRole.PlayersWordsCollection.GlobalRole.Spectator
                                    },
                                ),
                                playersList = playersList,
                                settings = settings,
                                extraSettings = nextState.extraSettings,
                            )
                        )
                    }
                }
                is GameStateMachine.State.GameInitialised.Round.RoundWaiting -> {
                    val hostIndex = nextState.playersList.firstIndexThat { _, player -> player.isOnline }
                    val playersList = scope {
                        var playingPlayerIndex = 0u
                        nextState.playersList.mapIndexed { index, player ->
                            Outgoing.PlayerDescription.Round.Waiting(
                                metadata = player.metadata,
                                isOnline = player.isOnline,
                                isHost = index == hostIndex,
                                globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                    GlobalRole.Player -> Outgoing.PlayerDescription.Round.GlobalRole.Player(
                                        when (playingPlayerIndex) {
                                            nextGameState.speakerIndex -> Outgoing.PlayerDescription.Round.GlobalRole.Player.RoundRole.Speaker
                                            nextGameState.listenerIndex -> Outgoing.PlayerDescription.Round.GlobalRole.Player.RoundRole.Listener
                                            else -> null
                                        },
                                    ).also { playingPlayerIndex++ }
                                    GlobalRole.Spectator -> Outgoing.PlayerDescription.Round.GlobalRole.Spectator
                                },
                            )
                        }
                    }
                    val settings = GameSettings(
                        preparationTimeSeconds = nextGameState.settings.preparationTimeSeconds,
                        explanationTimeSeconds = nextGameState.settings.explanationTimeSeconds,
                        finalGuessTimeSeconds = nextGameState.settings.finalGuessTimeSeconds,
                        strictMode = nextGameState.settings.strictMode,
                        gameEndCondition = nextGameState.settings.gameEndCondition,
                        wordsSource = when (val wordsSource = nextGameState.settings.wordsSource) {
                            GameStateMachine.WordsSource.Players -> WordsSource.Players
                            is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                        },
                    )
                    var playingPlayerIndex = 0u
                    nextState.playersList.forEachIndexed { index, player ->
                        player.sendNewState(
                            Outgoing.State.Round.Waiting(
                                roomMetadata = metadata,
                                selfRole = Outgoing.SelfRole.Round.Waiting(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isHost = index == hostIndex,
                                    globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                        GlobalRole.Player -> Outgoing.SelfRole.Round.Waiting.GlobalRole.Player(
                                            when (playingPlayerIndex) {
                                                nextGameState.speakerIndex -> Outgoing.SelfRole.Round.Waiting.GlobalRole.Player.RoundRole.Speaker
                                                nextGameState.listenerIndex -> Outgoing.SelfRole.Round.Waiting.GlobalRole.Player.RoundRole.Listener
                                                else -> null
                                            },
                                            roundsBeforeSpeaking = nextGameState.playersRoundsBeforeSpeaking[playingPlayerIndex],
                                            roundsBeforeListening = nextGameState.playersRoundsBeforeListening[playingPlayerIndex],
                                        ).also { playingPlayerIndex++ }
                                        GlobalRole.Spectator -> Outgoing.SelfRole.Round.Waiting.GlobalRole.Spectator
                                    },
                                    isGameFinishable = index == hostIndex,
                                ),
                                playersList = playersList,
                                settings = settings,
                                extraSettings = nextState.extraSettings,
                                initialWordsNumber = nextGameState.initialWordsNumber,
                                roundNumber =  nextGameState.roundNumber,
                                cycleNumber = nextGameState.cycleNumber,
                                speakerIndex = nextGameState.speakerIndex,
                                listenerIndex = nextGameState.listenerIndex,
                                nextSpeakerIndex = nextGameState.nextSpeakerIndex,
                                nextListenerIndex = nextGameState.nextListenerIndex,
                                restWordsNumber = nextGameState.restWords.size,
                                wordsInProgressNumber = 0u,
                                wordsStatistic =
                                    if (nextState.extraSettings.showWordsStatistic) nextGameState.wordsStatistic.toOutgoingAPI().filter { it.state != GameStateMachine.WordStatistic.State.InProgress }
                                    else null,
                                speakerReady = nextGameState.speakerReady,
                                listenerReady = nextGameState.listenerReady,
                                leaderboard =
                                    if (nextState.extraSettings.showLeaderboardPermutation)
                                        Outgoing.Leaderboard(
                                            permutation =
                                                nextGameState.explanationScores
                                                    .indices
                                                    .sortedByDescending { nextGameState.explanationScores[it] + nextGameState.guessingScores[it] }
                                                    .toKoneUIntArray(),
                                            scoreExplained = nextGameState.explanationScores.toKoneUIntArray(),
                                            scoreGuessed = nextGameState.guessingScores.toKoneUIntArray(),
                                            scoreSum = KoneUIntArray.generate(playersList.size) { nextGameState.explanationScores[it] + nextGameState.guessingScores[it] },
                                        )
                                    else null
                                ,
                            )
                        )
                    }
                }
                is GameStateMachine.State.GameInitialised.Round.RoundPreparation -> {
                    val hostIndex = nextState.playersList.firstIndexThat { _, player -> player.isOnline }
                    val playersList = scope {
                        var playingPlayerIndex = 0u
                        nextState.playersList.mapIndexed { index, player ->
                            Outgoing.PlayerDescription.Round.Preparation(
                                metadata = player.metadata,
                                isOnline = player.isOnline,
                                isHost = index == hostIndex,
                                globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                    GlobalRole.Player -> Outgoing.PlayerDescription.Round.GlobalRole.Player(
                                        when (playingPlayerIndex) {
                                            nextGameState.speakerIndex -> Outgoing.PlayerDescription.Round.GlobalRole.Player.RoundRole.Speaker
                                            nextGameState.listenerIndex -> Outgoing.PlayerDescription.Round.GlobalRole.Player.RoundRole.Listener
                                            else -> null
                                        },
                                    ).also { playingPlayerIndex++ }
                                    GlobalRole.Spectator -> Outgoing.PlayerDescription.Round.GlobalRole.Spectator
                                },
                            )
                        }
                    }
                    val settings = GameSettings(
                        preparationTimeSeconds = nextGameState.settings.preparationTimeSeconds,
                        explanationTimeSeconds = nextGameState.settings.explanationTimeSeconds,
                        finalGuessTimeSeconds = nextGameState.settings.finalGuessTimeSeconds,
                        strictMode = nextGameState.settings.strictMode,
                        gameEndCondition = nextGameState.settings.gameEndCondition,
                        wordsSource = when (val wordsSource = nextGameState.settings.wordsSource) {
                            GameStateMachine.WordsSource.Players -> WordsSource.Players
                            is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                        },
                    )
                    var playingPlayerIndex = 0u
                    nextState.playersList.forEachIndexed { index, player ->
                        player.sendNewState(
                            Outgoing.State.Round.Preparation(
                                roomMetadata = metadata,
                                selfRole = Outgoing.SelfRole.Round.Preparation(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isHost = index == hostIndex,
                                    globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                        GlobalRole.Player -> Outgoing.SelfRole.Round.Preparation.GlobalRole.Player(
                                            when (playingPlayerIndex) {
                                                nextGameState.speakerIndex -> Outgoing.SelfRole.Round.Preparation.GlobalRole.Player.RoundRole.Speaker
                                                nextGameState.listenerIndex -> Outgoing.SelfRole.Round.Preparation.GlobalRole.Player.RoundRole.Listener
                                                else -> null
                                            },
                                            roundsBeforeSpeaking = nextGameState.playersRoundsBeforeSpeaking[playingPlayerIndex],
                                            roundsBeforeListening = nextGameState.playersRoundsBeforeListening[playingPlayerIndex],
                                        ).also { playingPlayerIndex++ }
                                        GlobalRole.Spectator -> Outgoing.SelfRole.Round.Preparation.GlobalRole.Spectator
                                    },
                                ),
                                playersList = playersList,
                                settings = settings,
                                extraSettings = nextState.extraSettings,
                                initialWordsNumber = nextGameState.initialWordsNumber,
                                roundNumber =  nextGameState.roundNumber,
                                cycleNumber = nextGameState.cycleNumber,
                                speakerIndex = nextGameState.speakerIndex,
                                listenerIndex = nextGameState.listenerIndex,
                                nextSpeakerIndex = nextGameState.nextSpeakerIndex,
                                nextListenerIndex = nextGameState.nextListenerIndex,
                                restWordsNumber = nextGameState.restWords.size,
                                wordsInProgressNumber = 0u,
                                wordsStatistic =
                                    if (nextState.extraSettings.showWordsStatistic) nextGameState.wordsStatistic.toOutgoingAPI().filter { it.state != GameStateMachine.WordStatistic.State.InProgress }
                                    else null,
                                millisecondsLeft = nextGameState.millisecondsLeft,
                                leaderboard =
                                    if (nextState.extraSettings.showLeaderboardPermutation)
                                        Outgoing.Leaderboard(
                                            permutation =
                                                playersList
                                                    .indices
                                                    .sortedByDescending { nextGameState.explanationScores[it] + nextGameState.guessingScores[it] }
                                                    .toKoneUIntArray(),
                                            scoreExplained = nextGameState.explanationScores.toKoneUIntArray(),
                                            scoreGuessed = nextGameState.guessingScores.toKoneUIntArray(),
                                            scoreSum = KoneUIntArray.generate(playersList.size) { nextGameState.explanationScores[it] + nextGameState.guessingScores[it] },
                                        )
                                    else null
                                ,
                            )
                        )
                    }
                }
                is GameStateMachine.State.GameInitialised.Round.RoundExplanation -> {
                    val hostIndex = nextState.playersList.firstIndexThat { _, player -> player.isOnline }
                    val playersList = scope {
                        var playingPlayerIndex = 0u
                        nextState.playersList.mapIndexed { index, player ->
                            Outgoing.PlayerDescription.Round.Explanation(
                                metadata = player.metadata,
                                isOnline = player.isOnline,
                                isHost = index == hostIndex,
                                globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                    GlobalRole.Player -> Outgoing.PlayerDescription.Round.GlobalRole.Player(
                                        when (playingPlayerIndex) {
                                            nextGameState.speakerIndex -> Outgoing.PlayerDescription.Round.GlobalRole.Player.RoundRole.Speaker
                                            nextGameState.listenerIndex -> Outgoing.PlayerDescription.Round.GlobalRole.Player.RoundRole.Listener
                                            else -> null
                                        },
                                    ).also { playingPlayerIndex++ }
                                    GlobalRole.Spectator -> Outgoing.PlayerDescription.Round.GlobalRole.Spectator
                                },
                            )
                        }
                    }
                    val settings = GameSettings(
                        preparationTimeSeconds = nextGameState.settings.preparationTimeSeconds,
                        explanationTimeSeconds = nextGameState.settings.explanationTimeSeconds,
                        finalGuessTimeSeconds = nextGameState.settings.finalGuessTimeSeconds,
                        strictMode = nextGameState.settings.strictMode,
                        gameEndCondition = nextGameState.settings.gameEndCondition,
                        wordsSource = when (val wordsSource = nextGameState.settings.wordsSource) {
                            GameStateMachine.WordsSource.Players -> WordsSource.Players
                            is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                        },
                    )
                    var playingPlayerIndex = 0u
                    nextState.playersList.forEachIndexed { index, player ->
                        player.sendNewState(
                            Outgoing.State.Round.Explanation(
                                roomMetadata = metadata,
                                selfRole = Outgoing.SelfRole.Round.Explanation(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isHost = index == hostIndex,
                                    globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                        GlobalRole.Player -> Outgoing.SelfRole.Round.Explanation.GlobalRole.Player(
                                            roundRole = when (playingPlayerIndex) {
                                                nextGameState.speakerIndex -> Outgoing.SelfRole.Round.Explanation.GlobalRole.Player.RoundRole.Speaker(nextGameState.currentWord)
                                                nextGameState.listenerIndex -> Outgoing.SelfRole.Round.Explanation.GlobalRole.Player.RoundRole.Listener
                                                else -> null
                                            },
                                            roundsBeforeSpeaking = nextGameState.playersRoundsBeforeSpeaking[playingPlayerIndex],
                                            roundsBeforeListening = nextGameState.playersRoundsBeforeListening[playingPlayerIndex],
                                        ).also { playingPlayerIndex++ }
                                        GlobalRole.Spectator -> Outgoing.SelfRole.Round.Explanation.GlobalRole.Spectator
                                    },
                                ),
                                playersList = playersList,
                                settings = settings,
                                extraSettings = nextState.extraSettings,
                                initialWordsNumber = nextGameState.initialWordsNumber,
                                roundNumber =  nextGameState.roundNumber,
                                cycleNumber = nextGameState.cycleNumber,
                                speakerIndex = nextGameState.speakerIndex,
                                listenerIndex = nextGameState.listenerIndex,
                                nextSpeakerIndex = nextGameState.nextSpeakerIndex,
                                nextListenerIndex = nextGameState.nextListenerIndex,
                                restWordsNumber = nextGameState.restWords.size,
                                wordsInProgressNumber = 0u,
                                wordsStatistic =
                                    if (nextState.extraSettings.showWordsStatistic) nextGameState.wordsStatistic.toOutgoingAPI().filter { it.state != GameStateMachine.WordStatistic.State.InProgress }
                                    else null,
                                millisecondsLeft = nextGameState.millisecondsLeft,
                                leaderboard =
                                    if (nextState.extraSettings.showLeaderboardPermutation)
                                        Outgoing.Leaderboard(
                                            permutation =
                                                playersList
                                                    .indices
                                                    .sortedByDescending { nextGameState.explanationScores[it] + nextGameState.guessingScores[it] }
                                                    .toKoneUIntArray(),
                                            scoreExplained = nextGameState.explanationScores.toKoneUIntArray(),
                                            scoreGuessed = nextGameState.guessingScores.toKoneUIntArray(),
                                            scoreSum = KoneUIntArray.generate(playersList.size) { nextGameState.explanationScores[it] + nextGameState.guessingScores[it] },
                                        )
                                    else null
                                ,
                            )
                        )
                    }
                }
                is GameStateMachine.State.GameInitialised.Round.RoundLastGuess -> {
                    val hostIndex = nextState.playersList.firstIndexThat { _, player -> player.isOnline }
                    val playersList = scope {
                        var playingPlayerIndex = 0u
                        nextState.playersList.mapIndexed { index, player ->
                            Outgoing.PlayerDescription.Round.LastGuess(
                                metadata = player.metadata,
                                isOnline = player.isOnline,
                                isHost = index == hostIndex,
                                globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                    GlobalRole.Player -> Outgoing.PlayerDescription.Round.GlobalRole.Player(
                                        when (playingPlayerIndex) {
                                            nextGameState.speakerIndex -> Outgoing.PlayerDescription.Round.GlobalRole.Player.RoundRole.Speaker
                                            nextGameState.listenerIndex -> Outgoing.PlayerDescription.Round.GlobalRole.Player.RoundRole.Listener
                                            else -> null
                                        },
                                    ).also { playingPlayerIndex++ }
                                    GlobalRole.Spectator -> Outgoing.PlayerDescription.Round.GlobalRole.Spectator
                                },
                            )
                        }
                    }
                    val settings = GameSettings(
                        preparationTimeSeconds = nextGameState.settings.preparationTimeSeconds,
                        explanationTimeSeconds = nextGameState.settings.explanationTimeSeconds,
                        finalGuessTimeSeconds = nextGameState.settings.finalGuessTimeSeconds,
                        strictMode = nextGameState.settings.strictMode,
                        gameEndCondition = nextGameState.settings.gameEndCondition,
                        wordsSource = when (val wordsSource = nextGameState.settings.wordsSource) {
                            GameStateMachine.WordsSource.Players -> WordsSource.Players
                            is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                        },
                    )
                    var playingPlayerIndex = 0u
                    nextState.playersList.forEachIndexed { index, player ->
                        player.sendNewState(
                            Outgoing.State.Round.LastGuess(
                                roomMetadata = metadata,
                                selfRole = Outgoing.SelfRole.Round.LastGuess(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isHost = index == hostIndex,
                                    globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                        GlobalRole.Player -> Outgoing.SelfRole.Round.LastGuess.GlobalRole.Player(
                                            roundRole = when (playingPlayerIndex) {
                                                nextGameState.speakerIndex -> Outgoing.SelfRole.Round.LastGuess.GlobalRole.Player.RoundRole.Speaker(nextGameState.currentWord)
                                                nextGameState.listenerIndex -> Outgoing.SelfRole.Round.LastGuess.GlobalRole.Player.RoundRole.Listener
                                                else -> null
                                            },
                                            roundsBeforeSpeaking = nextGameState.playersRoundsBeforeSpeaking[playingPlayerIndex],
                                            roundsBeforeListening = nextGameState.playersRoundsBeforeListening[playingPlayerIndex],
                                        ).also { playingPlayerIndex++ }
                                        GlobalRole.Spectator -> Outgoing.SelfRole.Round.LastGuess.GlobalRole.Spectator
                                    },
                                ),
                                playersList = playersList,
                                settings = settings,
                                extraSettings = nextState.extraSettings,
                                initialWordsNumber = nextGameState.initialWordsNumber,
                                roundNumber =  nextGameState.roundNumber,
                                cycleNumber = nextGameState.cycleNumber,
                                speakerIndex = nextGameState.speakerIndex,
                                listenerIndex = nextGameState.listenerIndex,
                                nextSpeakerIndex = nextGameState.nextSpeakerIndex,
                                nextListenerIndex = nextGameState.nextListenerIndex,
                                restWordsNumber = nextGameState.restWords.size,
                                wordsInProgressNumber = 0u,
                                wordsStatistic =
                                    if (nextState.extraSettings.showWordsStatistic) nextGameState.wordsStatistic.toOutgoingAPI().filter { it.state != GameStateMachine.WordStatistic.State.InProgress }
                                    else null,
                                millisecondsLeft = nextGameState.millisecondsLeft,
                                leaderboard =
                                    if (nextState.extraSettings.showLeaderboardPermutation)
                                        Outgoing.Leaderboard(
                                            permutation =
                                                playersList
                                                    .indices
                                                    .sortedByDescending { nextGameState.explanationScores[it] + nextGameState.guessingScores[it] }
                                                    .toKoneUIntArray(),
                                            scoreExplained = nextGameState.explanationScores.toKoneUIntArray(),
                                            scoreGuessed = nextGameState.guessingScores.toKoneUIntArray(),
                                            scoreSum = KoneUIntArray.generate(playersList.size) { nextGameState.explanationScores[it] + nextGameState.guessingScores[it] },
                                        )
                                    else null
                                ,
                            )
                        )
                    }
                }
                is GameStateMachine.State.GameInitialised.Round.RoundEditing -> {
                    val hostIndex = nextState.playersList.firstIndexThat { _, player -> player.isOnline }
                    val playersList = scope {
                        var playingPlayerIndex = 0u
                        nextState.playersList.mapIndexed { index, player ->
                            Outgoing.PlayerDescription.Round.Editing(
                                metadata = player.metadata,
                                isOnline = player.isOnline,
                                isHost = index == hostIndex,
                                globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                    GlobalRole.Player -> Outgoing.PlayerDescription.Round.GlobalRole.Player(
                                        when (playingPlayerIndex) {
                                            nextGameState.speakerIndex -> Outgoing.PlayerDescription.Round.GlobalRole.Player.RoundRole.Speaker
                                            nextGameState.listenerIndex -> Outgoing.PlayerDescription.Round.GlobalRole.Player.RoundRole.Listener
                                            else -> null
                                        },
                                    ).also { playingPlayerIndex++ }
                                    GlobalRole.Spectator -> Outgoing.PlayerDescription.Round.GlobalRole.Spectator
                                },
                            )
                        }
                    }
                    val settings = GameSettings(
                        preparationTimeSeconds = nextGameState.settings.preparationTimeSeconds,
                        explanationTimeSeconds = nextGameState.settings.explanationTimeSeconds,
                        finalGuessTimeSeconds = nextGameState.settings.finalGuessTimeSeconds,
                        strictMode = nextGameState.settings.strictMode,
                        gameEndCondition = nextGameState.settings.gameEndCondition,
                        wordsSource = when (val wordsSource = nextGameState.settings.wordsSource) {
                            GameStateMachine.WordsSource.Players -> WordsSource.Players
                            is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                        },
                    )
                    var playingPlayerIndex = 0u
                    nextState.playersList.forEachIndexed { index, player ->
                        player.sendNewState(
                            Outgoing.State.Round.Editing(
                                roomMetadata = metadata,
                                selfRole = Outgoing.SelfRole.Round.Editing(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isHost = index == hostIndex,
                                    globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                        GlobalRole.Player -> Outgoing.SelfRole.Round.Editing.GlobalRole.Player(
                                            roundRole = when (playingPlayerIndex) {
                                                nextGameState.speakerIndex -> Outgoing.SelfRole.Round.Editing.GlobalRole.Player.RoundRole.Speaker(nextGameState.currentExplanationResults)
                                                nextGameState.listenerIndex -> Outgoing.SelfRole.Round.Editing.GlobalRole.Player.RoundRole.Listener
                                                else -> null
                                            },
                                            roundsBeforeSpeaking = nextGameState.playersRoundsBeforeSpeaking[playingPlayerIndex],
                                            roundsBeforeListening = nextGameState.playersRoundsBeforeListening[playingPlayerIndex],
                                        ).also { playingPlayerIndex++ }
                                        GlobalRole.Spectator -> Outgoing.SelfRole.Round.Editing.GlobalRole.Spectator
                                    },
                                ),
                                playersList = playersList,
                                settings = settings,
                                extraSettings = nextState.extraSettings,
                                initialWordsNumber = nextGameState.initialWordsNumber,
                                roundNumber =  nextGameState.roundNumber,
                                cycleNumber = nextGameState.cycleNumber,
                                speakerIndex = nextGameState.speakerIndex,
                                listenerIndex = nextGameState.listenerIndex,
                                nextSpeakerIndex = nextGameState.nextSpeakerIndex,
                                nextListenerIndex = nextGameState.nextListenerIndex,
                                restWordsNumber = nextGameState.restWords.size,
                                wordsInProgressNumber = 0u,
                                wordsStatistic =
                                    if (nextState.extraSettings.showWordsStatistic) nextGameState.wordsStatistic.toOutgoingAPI().filter { it.state != GameStateMachine.WordStatistic.State.InProgress }
                                    else null,
                                leaderboard =
                                    if (nextState.extraSettings.showLeaderboardPermutation)
                                        Outgoing.Leaderboard(
                                            permutation =
                                                playersList
                                                    .indices
                                                    .sortedByDescending { nextGameState.explanationScores[it] + nextGameState.guessingScores[it] }
                                                    .toKoneUIntArray(),
                                            scoreExplained = nextGameState.explanationScores.toKoneUIntArray(),
                                            scoreGuessed = nextGameState.guessingScores.toKoneUIntArray(),
                                            scoreSum = KoneUIntArray.generate(playersList.size) { nextGameState.explanationScores[it] + nextGameState.guessingScores[it] },
                                        )
                                    else null
                                ,
                            )
                        )
                    }
                }
                is GameStateMachine.State.GameInitialised.GameResults -> {
                    val hostIndex = nextState.playersList.firstIndexThat { _, player -> player.isOnline }
                    val playersList = scope {
                        nextState.playersList.mapIndexed { index, player ->
                            Outgoing.PlayerDescription.GameResults(
                                metadata = player.metadata,
                                isOnline = player.isOnline,
                                isHost = index == hostIndex,
                                globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                    GlobalRole.Player -> Outgoing.PlayerDescription.GameResults.GlobalRole.Player
                                    GlobalRole.Spectator -> Outgoing.PlayerDescription.GameResults.GlobalRole.Spectator
                                },
                            )
                        }
                    }
                    val settings = GameSettings(
                        preparationTimeSeconds = nextGameState.settings.preparationTimeSeconds,
                        explanationTimeSeconds = nextGameState.settings.explanationTimeSeconds,
                        finalGuessTimeSeconds = nextGameState.settings.finalGuessTimeSeconds,
                        strictMode = nextGameState.settings.strictMode,
                        gameEndCondition = nextGameState.settings.gameEndCondition,
                        wordsSource = when (val wordsSource = nextGameState.settings.wordsSource) {
                            GameStateMachine.WordsSource.Players -> WordsSource.Players
                            is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                        },
                    )
                    nextState.playersList.forEachIndexed { index, player ->
                        player.sendNewState(
                            Outgoing.State.GameResults(
                                roomMetadata = metadata,
                                selfRole = Outgoing.SelfRole.GameResults(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isHost = index == hostIndex,
                                    globalRole = when (val globalRole = nextState.globalRoles[index]) {
                                        GlobalRole.Player -> Outgoing.SelfRole.GameResults.GlobalRole.Player
                                        GlobalRole.Spectator -> Outgoing.SelfRole.GameResults.GlobalRole.Spectator
                                    },
                                ),
                                playersList = playersList,
                                settings = settings,
                                extraSettings = nextState.extraSettings,
                                wordsStatistic = nextGameState.wordsStatistic.toOutgoingAPI().filter { it.state != GameStateMachine.WordStatistic.State.InProgress },
                                leaderboard = Outgoing.Leaderboard(
                                    permutation =
                                        playersList
                                            .indices
                                            .sortedByDescending { nextGameState.explanationScores[it] + nextGameState.guessingScores[it] }
                                            .toKoneUIntArray(),
                                    scoreExplained = nextGameState.explanationScores.toKoneUIntArray(),
                                    scoreGuessed = nextGameState.guessingScores.toKoneUIntArray(),
                                    scoreSum = KoneUIntArray.generate(playersList.size) { nextGameState.explanationScores[it] + nextGameState.guessingScores[it] },
                                ),
                            )
                        )
                    }
                }
            }
        }
    }
    
    public val description: Description<RoomMetadata, PlayerMetadata>
        get() {
            val state = stateMachine.state
            val playersList = when (state) {
                is State.RoomPlayersGathering -> state.appearedPlayersList.filter { it.isOnline }
                is State.GameInitialization -> state.playersList
                is State.GameInProgress -> state.playersList
            }
            val hostIndex = playersList.firstIndexThat { _, player -> player.isOnline }
            val playersDescriptionsList = playersList.mapIndexed { index, player ->
                Player.Description(metadata = player.metadata, isOnline = player.isOnline, isHost = index == hostIndex)
            }
            return Description(
                metadata = metadata,
                playersList = playersDescriptionsList,
                stateType = when (state) {
                    is State.RoomPlayersGathering -> StateType.RoomPlayersGathering
                    is State.GameInitialization -> StateType.GameInitialisation
                    is State.GameInProgress -> when (state.gameState) {
                        is GameStateMachine.State.GameInitialised.PlayersWordsCollection -> StateType.PlayersWordsCollection
                        is GameStateMachine.State.GameInitialised.Round.RoundWaiting -> StateType.RoundWaiting
                        is GameStateMachine.State.GameInitialised.Round.RoundPreparation -> StateType.RoundPreparation
                        is GameStateMachine.State.GameInitialised.Round.RoundExplanation -> StateType.RoundExplanation
                        is GameStateMachine.State.GameInitialised.Round.RoundLastGuess -> StateType.RoundLastGuess
                        is GameStateMachine.State.GameInitialised.Round.RoundEditing -> StateType.RoundEditing
                        is GameStateMachine.State.GameInitialised.GameResults -> StateType.GameResults
                    }
                }
            )
        }
    
    public suspend fun attachConnectionToPlayer(connection: ConnectionType, playerID: PlayerID): Player.Attachment<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>? {
        val result = stateMachine.moveMaybeAndCompute { previousState ->
            val player = when (previousState) {
                is State.RoomPlayersGathering -> previousState.appearedPlayersList
                is State.GameInitialization -> previousState.playersList
                is State.GameInProgress -> previousState.playersList
            }.firstThatOrNull { it.metadata.id == playerID }

            if (player == null) {
                val metadata = initialMetadataFactory(playerID)
                if (checkConnectionAttachment(metadata, false, connection)) {
                    val newPlayer = Player(this, metadata)
                    val node = newPlayer.connectionsRegistry.addNode(connection)
                    val attachment = Player.Attachment(newPlayer, node)

                    TransitionOrReasonAndComputation.Success(
                        Transition.AddPlayer(newPlayer = newPlayer),
                        attachment,
                    )
                } else {
                    TransitionOrReasonAndComputation.Failure(Outgoing.Error.AttachmentIsDenied, null)
                }
            } else {
                if (checkConnectionAttachment(player.metadata, player.isOnline, connection)) {
                    val node = player.connectionsRegistry.addNode(connection)
                    val attachment = Player.Attachment(player, node)

                    TransitionOrReasonAndComputation.Success(
                        Transition.UpdatePlayersState(),
                        attachment,
                    )
                } else {
                    TransitionOrReasonAndComputation.Failure(Outgoing.Error.AttachmentIsDenied, null)
                }
            }
        }
        return when (result) {
            is MovementMaybeAndComputationResult.NoTransition -> {
                connection.sendError(result.noTransitionReason)
                null
            }
            is MovementMaybeAndComputationResult.NoNextState -> {
                connection.sendError(result.noNextStateReason)
                null
            }
            is MovementMaybeAndComputationResult.Success -> result.computation
        }
    }
}