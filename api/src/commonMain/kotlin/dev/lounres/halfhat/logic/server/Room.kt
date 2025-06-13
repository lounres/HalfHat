package dev.lounres.halfhat.logic.server

import dev.lounres.halfhat.logic.gameStateMachine.AsynchronousGameStateMachine
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.halfhat.logic.gameStateMachine.Initialization
import dev.lounres.halfhat.logic.gameStateMachine.moveMaybe
import dev.lounres.halfhat.logic.gameStateMachine.moveMaybeAndCompute
import dev.lounres.halfhat.logic.gameStateMachine.confirmWordsExplanationResults
import dev.lounres.halfhat.logic.gameStateMachine.listenerReady
import dev.lounres.halfhat.logic.gameStateMachine.speakerReady
import dev.lounres.halfhat.logic.gameStateMachine.state
import dev.lounres.halfhat.logic.gameStateMachine.updateWordsExplanationResults
import dev.lounres.halfhat.logic.gameStateMachine.wordExplanationState
import dev.lounres.halfhat.logic.server.Room.GameStateMachineMetadataTransition.*
import dev.lounres.kone.automata.CheckResult
import dev.lounres.kone.automata.MovementMaybeResult
import dev.lounres.kone.automata.MovementResult
import dev.lounres.kone.automata.TransitionOrReason
import dev.lounres.kone.automata.TransitionOrReasonAndComputation
import dev.lounres.kone.collections.iterables.isNotEmpty
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.KoneMutableList
import dev.lounres.kone.collections.list.KoneMutableListNode
import dev.lounres.kone.collections.list.KoneMutableNoddedList
import dev.lounres.kone.collections.list.empty
import dev.lounres.kone.collections.list.implementations.KoneGCLinkedList
import dev.lounres.kone.collections.utils.filter
import dev.lounres.kone.collections.utils.firstIndexThat
import dev.lounres.kone.collections.utils.firstThat
import dev.lounres.kone.collections.utils.firstThatOrNull
import dev.lounres.kone.collections.utils.forEach
import dev.lounres.kone.collections.utils.forEachIndexed
import dev.lounres.kone.collections.utils.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlin.random.Random


public class Room<
    RoomMetadata,
    PlayerID,
    PlayerMetadata : Room.Player.Metadata<PlayerID>,
    WordsProviderID,
    WordsProviderType: Room.WordsProvider<WordsProviderID>,
    ConnectionType: Room.Connection<RoomMetadata, PlayerMetadata, WordsProviderID>,
>(
    public val metadata: RoomMetadata,
    public val wordsProviderRegistry: WordProviderRegistry<WordsProviderID, WordsProviderType>,
    initialSettingsBuilder: GameSettings.Builder<WordsProviderID>,
    private val initialMetadataFactory: (PlayerID) -> PlayerMetadata,
    private val checkConnectionAttachment: (metadata: PlayerMetadata, isOnline: Boolean, connection: ConnectionType) -> Boolean,
) {
    public class Player<
        RoomMetadata,
        PlayerID,
        PlayerMetadata : Player.Metadata<PlayerID>,
        WordsProviderID,
        WordsProviderType: WordsProvider<WordsProviderID>,
        ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID>
    > internal constructor(
        private val room: Room<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>,
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
            WordsProviderType: WordsProvider<WordsProviderID>,
            ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID>
        > internal constructor(
            public val player: Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>,
            private val node: KoneMutableListNode<ConnectionType>,
        ) {
            public suspend fun sever() {
                player.room.gameStateMachine.moveMaybe { previousState ->
                    val isNodeActuallyAttached = !node.isDetached
                    node.remove()
                    
                    if (isNodeActuallyAttached)
                        TransitionOrReason.Success(
                            when (previousState) {
                                is GameStateMachine.State.GameInitialisation<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>, WordsProviderType, *> ->
                                    GameStateMachine.Transition.UpdateGame.UpdateGameSettings(
                                        playersList = previousState.metadata.allPlayersList.filter { it.isOnline },
                                        settingsBuilder = previousState.settingsBuilder,
                                    )
                                is GameStateMachine.State.RoundWaiting<*, *, *>,
                                is GameStateMachine.State.RoundPreparation<*, *, *>,
                                is GameStateMachine.State.RoundExplanation<*, *, *>,
                                is GameStateMachine.State.RoundLastGuess<*, *, *>,
                                is GameStateMachine.State.RoundEditing<*, *, *>,
                                is GameStateMachine.State.GameResults<*, *>, ->
                                    GameStateMachine.Transition.UpdateMetadata(
                                        SeverConnectionAttachment
                                    )
                            }
                        )
                    else {
                        node.element.sendError(Outgoing.Error.AttachmentIsAlreadySevered)
                        TransitionOrReason.Failure(null)
                    }
                }
            }
            
            public suspend fun updateGameSettings(settingsBuilder: GameSettings.Builder<WordsProviderID>) {
                player.room.gameStateMachine.moveMaybe { previousState ->
                    if (previousState.metadata.allPlayersList.firstThat { it.isOnline } != this) {
                        node.element.sendError(Outgoing.Error.NotHostChangingGameSettings)
                        return@moveMaybe TransitionOrReason.Failure(null)
                    }
                    when (previousState) {
                        is GameStateMachine.State.GameInitialisation<*, WordsProviderType, *> ->
                            TransitionOrReason.Success(
                                GameStateMachine.Transition.UpdateGame.UpdateGameSettings(
                                    playersList = previousState.playersList,
                                    settingsBuilder = GameStateMachine.GameSettings.Builder(
                                        preparationTimeSeconds = settingsBuilder.preparationTimeSeconds,
                                        explanationTimeSeconds = settingsBuilder.explanationTimeSeconds,
                                        finalGuessTimeSeconds = settingsBuilder.finalGuessTimeSeconds,
                                        strictMode = settingsBuilder.strictMode,
                                        cachedEndConditionWordsNumber = settingsBuilder.cachedEndConditionWordsNumber,
                                        cachedEndConditionCyclesNumber = settingsBuilder.cachedEndConditionCyclesNumber,
                                        gameEndConditionType = settingsBuilder.gameEndConditionType,
                                        wordsSource = when (val wordsSource = settingsBuilder.wordsSource) {
                                            WordsSource.Players -> GameStateMachine.WordsSource.Players
                                            is WordsSource.ServerDictionary<WordsProviderID> -> GameStateMachine.WordsSource.Custom(player.room.wordsProviderRegistry[wordsSource.id])
                                        },
                                    ),
                                )
                            )
                        is GameStateMachine.State.RoundWaiting<*, *, *>,
                        is GameStateMachine.State.RoundPreparation<*, *, *>,
                        is GameStateMachine.State.RoundExplanation<*, *, *>,
                        is GameStateMachine.State.RoundLastGuess<*, *, *>,
                        is GameStateMachine.State.RoundEditing<*, *, *>,
                        is GameStateMachine.State.GameResults<*, *>, -> TransitionOrReason.Failure(null)
                    }
                }
            }
            
            public suspend fun initializeGame() {
                val result = player.room.gameStateMachine.moveMaybe { previousState ->
                    if (previousState.metadata.allPlayersList.firstThat { it.isOnline } != this) {
                        node.element.sendError(Outgoing.Error.NotHostChangingGameSettings)
                        return@moveMaybe TransitionOrReason.Failure(null)
                    }
                    TransitionOrReason.Success(GameStateMachine.Transition.UpdateGame.InitialiseGame)
                }
                when (result) {
                    is MovementMaybeResult.NoTransition -> TODO()
                    is MovementMaybeResult.NoNextState<*, *, *> -> TODO()
                    is MovementMaybeResult.Success<*, *> -> TODO()
                }
//                if (result is MovementResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?>>) {
//                    val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
//                    if (error != null) node.element.sendError(error)
//                }
            }
            
            public suspend fun speakerReady() {
                val result = player.room.gameStateMachine.speakerReady()
                if (result is MovementResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?>>) {
                    val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                    if (error != null) node.element.sendError(error)
                }
            }
            
            public suspend fun listenerReady() {
                val result = player.room.gameStateMachine.listenerReady()
                if (result is MovementResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?>>) {
                    val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                    if (error != null) node.element.sendError(error)
                }
            }
            
            public suspend fun wordExplanationState(state: GameStateMachine.WordExplanation.State) {
                val result = player.room.gameStateMachine.wordExplanationState(state)
                if (result is MovementResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?>>) {
                    val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                    if (error != null) node.element.sendError(error)
                }
            }
            
            public suspend fun updateWordsExplanationResults(newExplanationResults: KoneList<GameStateMachine.WordExplanation>) {
                val result = player.room.gameStateMachine.updateWordsExplanationResults(newExplanationResults)
                if (result is MovementResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?>>) {
                    val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                    if (error != null) node.element.sendError(error)
                }
            }
            
            public suspend fun confirmWordsExplanationResults() {
                val result = player.room.gameStateMachine.confirmWordsExplanationResults()
                if (result is MovementResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?>>) {
                    val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                    if (error != null) node.element.sendError(error)
                }
            }
            
            public suspend fun finishGame() {
                val result = player.room.gameStateMachine.confirmWordsExplanationResults()
                if (result is MovementResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?>>) {
                    val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                    if (error != null) node.element.sendError(error)
                }
            }
        }
        
        @Serializable
        public data class Description<out PlayerMetadata>(
            val metadata: PlayerMetadata,
            val isOnline: Boolean
        )
        
        internal val connectionsRegistry: KoneMutableNoddedList<ConnectionType> = KoneGCLinkedList()
        public val isOnline: Boolean get() = connectionsRegistry.isNotEmpty()
        public val description: Description<PlayerMetadata> get() = Description(metadata, isOnline)
    }
    
    @Serializable
    public data class Description<out RoomMetadata, out PlayerMetadata>(
        val metadata: RoomMetadata,
        val playersList: KoneList<Player.Description<PlayerMetadata>>,
        val stateType: StateType
    )
    
    @Serializable
    public enum class StateType {
        GameInitialisation,
        RoundWaiting,
        RoundPreparation,
        RoundExplanation,
        RoundLastGuess,
        RoundEditing,
        GameResults,
    }
    
    public interface Connection<in RoomMetadata, in PlayerMetadata, in WordsProviderID> {
        public suspend fun sendNewState(state: Outgoing.State<RoomMetadata, PlayerMetadata, WordsProviderID>)
        public suspend fun sendError(error: Outgoing.Error)
    }
    
    public interface WordsProvider<out WordsProviderID> : GameStateMachine.WordsProvider {
        public val id: WordsProviderID
    }
    
    public interface WordProviderRegistry<WordsProviderID, out WordsProviderType: WordsProvider<WordsProviderID>> {
        public operator fun contains(id: WordsProviderID): Boolean
        public operator fun get(id: WordsProviderID): WordsProviderType
    }
    
    @Serializable
    public sealed interface WordsSource<out WordsProviderID> {
        @Serializable
        public data object Players : WordsSource<Nothing>
//        @Serializable
//        public data object HostDictionary: WordsSource
        @Serializable
        public data class ServerDictionary<WordsProviderID>(
            public val id: WordsProviderID,
        ) : WordsSource<WordsProviderID>
    }
    
    @Serializable
    public data class GameSettings<out WordsProviderID>(
        val preparationTimeSeconds: UInt,
        val explanationTimeSeconds: UInt,
        val finalGuessTimeSeconds: UInt,
        val strictMode: Boolean,
        val gameEndCondition: GameStateMachine.GameEndCondition,
        val wordsSource: WordsSource<WordsProviderID>,
    ) {
        @Serializable
        public data class Builder<out WordsProviderID>(
            val preparationTimeSeconds: UInt,
            val explanationTimeSeconds: UInt,
            val finalGuessTimeSeconds: UInt,
            val strictMode: Boolean,
            val cachedEndConditionWordsNumber: UInt,
            val cachedEndConditionCyclesNumber: UInt,
            val gameEndConditionType: GameStateMachine.GameEndCondition.Type,
            val wordsSource: WordsSource<WordsProviderID>,
        )
    }
    
    public object Outgoing {
        @Serializable
        public sealed interface Role<out PlayerMetadata> {
            public val metadata: PlayerMetadata
            public val userIndex: UInt
            public val isHost: Boolean
            
            @Serializable
            public data class GameInitialisation<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isHost: Boolean,
            ) : Role<PlayerMetadata>
            
            @Serializable
            public data class RoundWaiting<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val roundRole: RoundRole,
            ) : Role<PlayerMetadata> {
                @Serializable
                public enum class RoundRole {
                    Player, Speaker, Listener,
                }
            }
            
            @Serializable
            public data class RoundPreparation<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val roundRole: RoundRole,
            ) : Role<PlayerMetadata> {
                @Serializable
                public enum class RoundRole {
                    Player, Speaker, Listener,
                }
            }
            
            @Serializable
            public data class RoundExplanation<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val roundRole: RoundRole,
            ) : Role<PlayerMetadata> {
                @Serializable
                public sealed interface RoundRole {
                    public data object Player : RoundRole
                    public data class Speaker(val currentWord: String) : RoundRole
                    public data object Listener : RoundRole
                }
            }
            
            @Serializable
            public data class RoundLastGuess<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val roundRole: RoundRole,
            ) : Role<PlayerMetadata> {
                @Serializable
                public sealed interface RoundRole {
                    public data object Player : RoundRole
                    public data class Speaker(val currentWord: String) : RoundRole
                    public data object Listener : RoundRole
                }
            }
            
            @Serializable
            public data class RoundEditing<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val roundRole: RoundRole,
            ) : Role<PlayerMetadata> {
                @Serializable
                public sealed interface RoundRole {
                    public data object Player : RoundRole
                    public data class Speaker(public val wordsToEdit: KoneList<GameStateMachine.WordExplanation>) : RoundRole
                    public data object Listener : RoundRole
                }
            }
            
            @Serializable
            public data class GameResults<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isHost: Boolean,
            ) : Role<PlayerMetadata>
        }
        
        @Serializable
        public sealed interface State<out RoomMetadata, out PlayerMetadata, out WordsProviderID> {
            public val roomMetadata: RoomMetadata
            public val role: Role<PlayerMetadata>
            
            @Serializable
            public data class GameInitialisation<out RoomMetadata, out PlayerMetadata, out WordsProviderID>(
                override val roomMetadata: RoomMetadata,
                override val role: Role.GameInitialisation<PlayerMetadata>,
                public val playersList: KoneList<Player.Description<PlayerMetadata>>,
                public val settingsBuilder: GameSettings.Builder<WordsProviderID>,
            ) : State<RoomMetadata, PlayerMetadata, WordsProviderID>
            
            @Serializable
            public data class RoundWaiting<out RoomMetadata, out PlayerMetadata, out WordsProviderID>(
                override val roomMetadata: RoomMetadata,
                override val role: Role.RoundWaiting<PlayerMetadata>,
                public val playersList: KoneList<Player.Description<PlayerMetadata>>,
                public val settings: GameSettings<WordsProviderID>,
                public val roundNumber: UInt,
                public val cycleNumber: UInt,
                public val speakerIndex: UInt,
                public val listenerIndex: UInt,
                public val explanationScores: KoneList<UInt>,
                public val guessingScores: KoneList<UInt>,
                public val speakerReady: Boolean,
                public val listenerReady: Boolean,
            ) : State<RoomMetadata, PlayerMetadata, WordsProviderID>
            
            @Serializable
            public data class RoundPreparation<out RoomMetadata, out PlayerMetadata, out WordsProviderID>(
                override val roomMetadata: RoomMetadata,
                override val role: Role.RoundPreparation<PlayerMetadata>,
                public val playersList: KoneList<Player.Description<PlayerMetadata>>,
                public val settings: GameSettings<WordsProviderID>,
                public val roundNumber: UInt,
                public val cycleNumber: UInt,
                public val speakerIndex: UInt,
                public val listenerIndex: UInt,
                public val millisecondsLeft: UInt,
                public val explanationScores: KoneList<UInt>,
                public val guessingScores: KoneList<UInt>,
            ) : State<RoomMetadata, PlayerMetadata, WordsProviderID>
            
            @Serializable
            public data class RoundExplanation<out RoomMetadata, out PlayerMetadata, out WordsProviderID>(
                override val roomMetadata: RoomMetadata,
                override val role: Role.RoundExplanation<PlayerMetadata>,
                public val playersList: KoneList<Player.Description<PlayerMetadata>>,
                public val settings: GameSettings<WordsProviderID>,
                public val roundNumber: UInt,
                public val cycleNumber: UInt,
                public val speakerIndex: UInt,
                public val listenerIndex: UInt,
                public val millisecondsLeft: UInt,
                public val explanationScores: KoneList<UInt>,
                public val guessingScores: KoneList<UInt>,
            ) : State<RoomMetadata, PlayerMetadata, WordsProviderID>
            
            @Serializable
            public data class RoundLastGuess<out RoomMetadata, out PlayerMetadata, out WordsProviderID>(
                override val roomMetadata: RoomMetadata,
                override val role: Role.RoundLastGuess<PlayerMetadata>,
                public val playersList: KoneList<Player.Description<PlayerMetadata>>,
                public val settings: GameSettings<WordsProviderID>,
                public val roundNumber: UInt,
                public val cycleNumber: UInt,
                public val speakerIndex: UInt,
                public val listenerIndex: UInt,
                public val millisecondsLeft: UInt,
                public val explanationScores: KoneList<UInt>,
                public val guessingScores: KoneList<UInt>,
            ) : State<RoomMetadata, PlayerMetadata, WordsProviderID>
            
            @Serializable
            public data class RoundEditing<out RoomMetadata, out PlayerMetadata, out WordsProviderID>(
                override val roomMetadata: RoomMetadata,
                override val role: Role.RoundEditing<PlayerMetadata>,
                public val playersList: KoneList<Player.Description<PlayerMetadata>>,
                public val settings: GameSettings<WordsProviderID>,
                public val roundNumber: UInt,
                public val cycleNumber: UInt,
                public val speakerIndex: UInt,
                public val listenerIndex: UInt,
                public val explanationScores: KoneList<UInt>,
                public val guessingScores: KoneList<UInt>,
            ) : State<RoomMetadata, PlayerMetadata, WordsProviderID>
            
            @Serializable
            public data class GameResults<out RoomMetadata, out PlayerMetadata, out WordsProviderID>(
                override val roomMetadata: RoomMetadata,
                override val role: Role.GameResults<PlayerMetadata>,
                public val playersList: KoneList<Player.Description<PlayerMetadata>>,
                public val results: KoneList<GameStateMachine.GameResult>,
            ) : State<RoomMetadata, PlayerMetadata, WordsProviderID>
        }
        
        public sealed interface Error {
            public data object AttachmentIsDenied : Error
            public data object AttachmentIsAlreadySevered : Error
            public data object NotHostChangingGameSettings : Error
            public data object CannotUpdateGameSettingsAfterInitialization : Error
            public data object NotEnoughPlayersForInitialization : Error
            public data object CannotInitializationGameSettingsAfterInitialization : Error
            public data object CannotSetSpeakerReadinessNotDuringRoundWaiting : Error
            public data object CannotSetListenerReadinessNotDuringRoundWaiting : Error
            public data object CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting : Error
            public data object CannotUpdateRoundInfoNotDuringTheRound : Error
            public data object CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess : Error
            public data object CannotUpdateWordExplanationResultsNotDuringRoundEditing : Error
            public data object CannotConfirmWordExplanationResultsNotDuringRoundEditing : Error
            public data object CannotFinishGameNotDuringRoundWaiting : Error
            
            public companion object {
                internal fun fromGameStateMachineNoNextStateReason(reason: GameStateMachine.NoNextStateReason<Nothing?>): Error? =
                    when (reason) {
                        is GameStateMachine.NoNextStateReason.NoMetadataUpdate<*> -> null
                        GameStateMachine.NoNextStateReason.CannotUpdateGameSettingsAfterInitialization -> CannotUpdateGameSettingsAfterInitialization
                        GameStateMachine.NoNextStateReason.NotEnoughPlayersForInitialization -> NotEnoughPlayersForInitialization
                        GameStateMachine.NoNextStateReason.CannotInitializationGameSettingsAfterInitialization -> CannotInitializationGameSettingsAfterInitialization
                        GameStateMachine.NoNextStateReason.CannotSetSpeakerReadinessNotDuringRoundWaiting -> CannotSetSpeakerReadinessNotDuringRoundWaiting
                        GameStateMachine.NoNextStateReason.CannotSetListenerReadinessNotDuringRoundWaiting -> CannotSetListenerReadinessNotDuringRoundWaiting
                        GameStateMachine.NoNextStateReason.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting -> CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting
                        GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound -> CannotUpdateRoundInfoNotDuringTheRound
                        GameStateMachine.NoNextStateReason.CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess -> CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess
                        GameStateMachine.NoNextStateReason.CannotUpdateWordExplanationResultsNotDuringRoundEditing -> CannotUpdateWordExplanationResultsNotDuringRoundEditing
                        GameStateMachine.NoNextStateReason.CannotConfirmWordExplanationResultsNotDuringRoundEditing -> CannotConfirmWordExplanationResultsNotDuringRoundEditing
                        GameStateMachine.NoNextStateReason.CannotFinishGameNotDuringRoundWaiting -> CannotFinishGameNotDuringRoundWaiting
                    }
            }
        }
    }
    
    private data class GameStateMachineMetadata<RoomMetadata, PlayerID, PlayerMetadata : Player.Metadata<PlayerID>, WordsProviderID, WordsProviderType: WordsProvider<WordsProviderID>, ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID>>(
        val allPlayersList: KoneMutableList<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>>,
    )
    
    private sealed interface GameStateMachineMetadataTransition<out RoomMetadata, out PlayerID, out PlayerMetadata : Player.Metadata<PlayerID>, out WordsProviderID, out WordsProviderType: WordsProvider<WordsProviderID>> {
        data object AttachConnection : GameStateMachineMetadataTransition<Nothing, Nothing, Nothing, Nothing, Nothing>
        data object SeverConnectionAttachment: GameStateMachineMetadataTransition<Nothing, Nothing, Nothing, Nothing, Nothing>
    }
    
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private val random: Random = Random
    
    private val gameStateMachine =
        AsynchronousGameStateMachine.Initialization(
            coroutineScope = coroutineScope,
            random = random,
            metadata = GameStateMachineMetadata<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>(
                allPlayersList = KoneGCLinkedList(),
            ),
            playersList = KoneList.empty<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>>(),
            settingsBuilder = GameStateMachine.GameSettings.Builder(
                preparationTimeSeconds = initialSettingsBuilder.preparationTimeSeconds,
                explanationTimeSeconds = initialSettingsBuilder.explanationTimeSeconds,
                finalGuessTimeSeconds = initialSettingsBuilder.finalGuessTimeSeconds,
                strictMode = initialSettingsBuilder.strictMode,
                cachedEndConditionWordsNumber = initialSettingsBuilder.cachedEndConditionWordsNumber,
                cachedEndConditionCyclesNumber = initialSettingsBuilder.cachedEndConditionCyclesNumber,
                gameEndConditionType = initialSettingsBuilder.gameEndConditionType,
                wordsSource = when (val source = initialSettingsBuilder.wordsSource) {
                    WordsSource.Players -> GameStateMachine.WordsSource.Players
                    is WordsSource.ServerDictionary<WordsProviderID> -> GameStateMachine.WordsSource.Custom(wordsProviderRegistry[source.id])
                },
            ),
            checkMetadataUpdate = { previousState, metadataTransition: GameStateMachineMetadataTransition<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType> ->
                when (metadataTransition) {
                    is AttachConnection -> {
                        when (previousState) {
                            is GameStateMachine.State.GameInitialisation<*, *, *> -> CheckResult.Failure(null)
                            is GameStateMachine.State.RoundWaiting<*, *, *>,
                            is GameStateMachine.State.RoundPreparation<*, *, *>,
                            is GameStateMachine.State.RoundExplanation<*, *, *>,
                            is GameStateMachine.State.RoundLastGuess<*, *, *>,
                            is GameStateMachine.State.RoundEditing<*, *, *>,
                            is GameStateMachine.State.GameResults<*, *>, -> CheckResult.Success(previousState.metadata)
                        }
                    }
                    SeverConnectionAttachment -> {
                        when (previousState) {
                            is GameStateMachine.State.GameInitialisation<*, *, *> -> CheckResult.Failure(null)
                            is GameStateMachine.State.RoundWaiting<*, *, *>,
                            is GameStateMachine.State.RoundPreparation<*, *, *>,
                            is GameStateMachine.State.RoundExplanation<*, *, *>,
                            is GameStateMachine.State.RoundLastGuess<*, *, *>,
                            is GameStateMachine.State.RoundEditing<*, *, *>,
                            is GameStateMachine.State.GameResults<*, *>, -> CheckResult.Success(previousState.metadata)
                        }
                    }
                }
            }
        ) { _, _, nextState ->
            val hostIndex = nextState.playersList.firstIndexThat { _, player -> player.isOnline }
            nextState.playersList.forEachIndexed { index, player ->
                val gameStateToSend = when (nextState) {
                    is GameStateMachine.State.GameInitialisation<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>, WordsProviderType, *> -> {
                        val gameMachineSettingsBuilder = nextState.settingsBuilder
                        Outgoing.State.GameInitialisation(
                            roomMetadata = metadata,
                            role = Outgoing.Role.GameInitialisation(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                            ),
                            playersList = nextState.playersList.map { it.description },
                            settingsBuilder = GameSettings.Builder(
                                preparationTimeSeconds = gameMachineSettingsBuilder.preparationTimeSeconds,
                                explanationTimeSeconds = gameMachineSettingsBuilder.explanationTimeSeconds,
                                finalGuessTimeSeconds = gameMachineSettingsBuilder.finalGuessTimeSeconds,
                                strictMode = gameMachineSettingsBuilder.strictMode,
                                cachedEndConditionWordsNumber = gameMachineSettingsBuilder.cachedEndConditionWordsNumber,
                                cachedEndConditionCyclesNumber = gameMachineSettingsBuilder.cachedEndConditionCyclesNumber,
                                gameEndConditionType = gameMachineSettingsBuilder.gameEndConditionType,
                                wordsSource = when (val wordsSource = gameMachineSettingsBuilder.wordsSource) {
                                    GameStateMachine.WordsSource.Players -> WordsSource.Players
                                    is GameStateMachine.WordsSource.Custom<WordsProviderType> -> WordsSource.ServerDictionary(wordsSource.provider.id)
                                }
                            )
                        )
                    }
                    is GameStateMachine.State.RoundWaiting<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>, WordsProviderType, *> -> {
                        val gameMachineSettings = nextState.settings
                        Outgoing.State.RoundWaiting(
                            roomMetadata = metadata,
                            role = Outgoing.Role.RoundWaiting(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                                roundRole = when (index) {
                                    nextState.speakerIndex -> Outgoing.Role.RoundWaiting.RoundRole.Speaker
                                    nextState.listenerIndex -> Outgoing.Role.RoundWaiting.RoundRole.Listener
                                    else -> Outgoing.Role.RoundWaiting.RoundRole.Player
                                }
                            ),
                            playersList = nextState.playersList.map { it.description },
                            settings = GameSettings(
                                preparationTimeSeconds = gameMachineSettings.preparationTimeSeconds,
                                explanationTimeSeconds = gameMachineSettings.explanationTimeSeconds,
                                finalGuessTimeSeconds = gameMachineSettings.finalGuessTimeSeconds,
                                strictMode = gameMachineSettings.strictMode,
                                gameEndCondition = gameMachineSettings.gameEndCondition,
                                wordsSource = when (val wordsSource = gameMachineSettings.wordsSource) {
                                    GameStateMachine.WordsSource.Players -> WordsSource.Players
                                    is GameStateMachine.WordsSource.Custom<WordsProviderType> -> WordsSource.ServerDictionary(wordsSource.provider.id)
                                },
                            ),
                            roundNumber =  nextState.roundNumber,
                            cycleNumber = nextState.cycleNumber,
                            speakerIndex = nextState.speakerIndex,
                            listenerIndex = nextState.listenerIndex,
                            explanationScores = nextState.explanationScores,
                            guessingScores = nextState.guessingScores,
                            speakerReady = nextState.speakerReady,
                            listenerReady = nextState.listenerReady,
                        )
                    }
                    is GameStateMachine.State.RoundPreparation<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>, WordsProviderType, *> -> {
                        val gameMachineSettings = nextState.settings
                        Outgoing.State.RoundPreparation(
                            roomMetadata = metadata,
                            role = Outgoing.Role.RoundPreparation(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                                roundRole = when (index) {
                                    nextState.speakerIndex -> Outgoing.Role.RoundPreparation.RoundRole.Speaker
                                    nextState.listenerIndex -> Outgoing.Role.RoundPreparation.RoundRole.Listener
                                    else -> Outgoing.Role.RoundPreparation.RoundRole.Player
                                }
                            ),
                            playersList = nextState.playersList.map { it.description },
                            settings = GameSettings(
                                preparationTimeSeconds = gameMachineSettings.preparationTimeSeconds,
                                explanationTimeSeconds = gameMachineSettings.explanationTimeSeconds,
                                finalGuessTimeSeconds = gameMachineSettings.finalGuessTimeSeconds,
                                strictMode = gameMachineSettings.strictMode,
                                gameEndCondition = gameMachineSettings.gameEndCondition,
                                wordsSource = when (val wordsSource = gameMachineSettings.wordsSource) {
                                    GameStateMachine.WordsSource.Players -> WordsSource.Players
                                    is GameStateMachine.WordsSource.Custom<WordsProviderType> -> WordsSource.ServerDictionary(wordsSource.provider.id)
                                },
                            ),
                            roundNumber =  nextState.roundNumber,
                            cycleNumber = nextState.cycleNumber,
                            speakerIndex = nextState.speakerIndex,
                            listenerIndex = nextState.listenerIndex,
                            millisecondsLeft = nextState.millisecondsLeft,
                            explanationScores = nextState.explanationScores,
                            guessingScores = nextState.guessingScores,
                        )
                    }
                    is GameStateMachine.State.RoundExplanation<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>, WordsProviderType, *> -> {
                        val gameMachineSettings = nextState.settings
                        Outgoing.State.RoundExplanation(
                            roomMetadata = metadata,
                            role = Outgoing.Role.RoundExplanation(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                                roundRole = when (index) {
                                    nextState.speakerIndex -> Outgoing.Role.RoundExplanation.RoundRole.Speaker(nextState.currentWord)
                                    nextState.listenerIndex -> Outgoing.Role.RoundExplanation.RoundRole.Listener
                                    else -> Outgoing.Role.RoundExplanation.RoundRole.Player
                                }
                            ),
                            playersList = nextState.playersList.map { it.description },
                            settings = GameSettings(
                                preparationTimeSeconds = gameMachineSettings.preparationTimeSeconds,
                                explanationTimeSeconds = gameMachineSettings.explanationTimeSeconds,
                                finalGuessTimeSeconds = gameMachineSettings.finalGuessTimeSeconds,
                                strictMode = gameMachineSettings.strictMode,
                                gameEndCondition = gameMachineSettings.gameEndCondition,
                                wordsSource = when (val wordsSource = gameMachineSettings.wordsSource) {
                                    GameStateMachine.WordsSource.Players -> WordsSource.Players
                                    is GameStateMachine.WordsSource.Custom<WordsProviderType> -> WordsSource.ServerDictionary(wordsSource.provider.id)
                                },
                            ),
                            roundNumber =  nextState.roundNumber,
                            cycleNumber = nextState.cycleNumber,
                            speakerIndex = nextState.speakerIndex,
                            listenerIndex = nextState.listenerIndex,
                            millisecondsLeft = nextState.millisecondsLeft,
                            explanationScores = nextState.explanationScores,
                            guessingScores = nextState.guessingScores,
                        )
                    }
                    is GameStateMachine.State.RoundLastGuess<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>, WordsProviderType, *> -> {
                        val gameMachineSettings = nextState.settings
                        Outgoing.State.RoundLastGuess(
                            roomMetadata = metadata,
                            role = Outgoing.Role.RoundLastGuess(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                                roundRole = when (index) {
                                    nextState.speakerIndex -> Outgoing.Role.RoundLastGuess.RoundRole.Speaker(nextState.currentWord)
                                    nextState.listenerIndex -> Outgoing.Role.RoundLastGuess.RoundRole.Listener
                                    else -> Outgoing.Role.RoundLastGuess.RoundRole.Player
                                }
                            ),
                            playersList = nextState.playersList.map { it.description },
                            settings = GameSettings(
                                preparationTimeSeconds = gameMachineSettings.preparationTimeSeconds,
                                explanationTimeSeconds = gameMachineSettings.explanationTimeSeconds,
                                finalGuessTimeSeconds = gameMachineSettings.finalGuessTimeSeconds,
                                strictMode = gameMachineSettings.strictMode,
                                gameEndCondition = gameMachineSettings.gameEndCondition,
                                wordsSource = when (val wordsSource = gameMachineSettings.wordsSource) {
                                    GameStateMachine.WordsSource.Players -> WordsSource.Players
                                    is GameStateMachine.WordsSource.Custom<WordsProviderType> -> WordsSource.ServerDictionary(wordsSource.provider.id)
                                },
                            ),
                            roundNumber =  nextState.roundNumber,
                            cycleNumber = nextState.cycleNumber,
                            speakerIndex = nextState.speakerIndex,
                            listenerIndex = nextState.listenerIndex,
                            millisecondsLeft = nextState.millisecondsLeft,
                            explanationScores = nextState.explanationScores,
                            guessingScores = nextState.guessingScores,
                        )
                    }
                    is GameStateMachine.State.RoundEditing<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>, WordsProviderType, *> -> {
                        val gameMachineSettings = nextState.settings
                        Outgoing.State.RoundEditing(
                            roomMetadata = metadata,
                            role = Outgoing.Role.RoundEditing(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                                roundRole = when (index) {
                                    nextState.speakerIndex -> Outgoing.Role.RoundEditing.RoundRole.Speaker(nextState.currentExplanationResults)
                                    nextState.listenerIndex -> Outgoing.Role.RoundEditing.RoundRole.Listener
                                    else -> Outgoing.Role.RoundEditing.RoundRole.Player
                                }
                            ),
                            playersList = nextState.playersList.map { it.description },
                            settings = GameSettings(
                                preparationTimeSeconds = gameMachineSettings.preparationTimeSeconds,
                                explanationTimeSeconds = gameMachineSettings.explanationTimeSeconds,
                                finalGuessTimeSeconds = gameMachineSettings.finalGuessTimeSeconds,
                                strictMode = gameMachineSettings.strictMode,
                                gameEndCondition = gameMachineSettings.gameEndCondition,
                                wordsSource = when (val wordsSource = gameMachineSettings.wordsSource) {
                                    GameStateMachine.WordsSource.Players -> WordsSource.Players
                                    is GameStateMachine.WordsSource.Custom<WordsProviderType> -> WordsSource.ServerDictionary(wordsSource.provider.id)
                                },
                            ),
                            roundNumber =  nextState.roundNumber,
                            cycleNumber = nextState.cycleNumber,
                            speakerIndex = nextState.speakerIndex,
                            listenerIndex = nextState.listenerIndex,
                            explanationScores = nextState.explanationScores,
                            guessingScores = nextState.guessingScores,
                        )
                    }
                    is GameStateMachine.State.GameResults<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>, *> -> {
                        Outgoing.State.GameResults(
                            roomMetadata = metadata,
                            role = Outgoing.Role.GameResults(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                            ),
                            playersList = nextState.playersList.map { it.description },
                            results = nextState.results
                        )
                    }
                }
                player.connectionsRegistry.forEach { it.sendNewState(gameStateToSend) }
            }
        }
    
    public val description: Description<RoomMetadata, PlayerMetadata>
        get() =
            Description(
                metadata = metadata,
                playersList = gameStateMachine.state.playersList.map { it.description },
                stateType = when(gameStateMachine.state) {
                    is GameStateMachine.State.GameInitialisation -> StateType.GameInitialisation
                    is GameStateMachine.State.RoundWaiting -> StateType.RoundWaiting
                    is GameStateMachine.State.RoundPreparation -> StateType.RoundPreparation
                    is GameStateMachine.State.RoundExplanation -> StateType.RoundExplanation
                    is GameStateMachine.State.RoundLastGuess -> StateType.RoundLastGuess
                    is GameStateMachine.State.RoundEditing -> StateType.RoundEditing
                    is GameStateMachine.State.GameResults -> StateType.GameResults
                }
            )
    
    public suspend fun attachConnectionToPlayer(connection: ConnectionType, playerID: PlayerID): Player.Attachment<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>? =
        gameStateMachine.moveMaybeAndCompute { previousState ->
            when (previousState) {
                is GameStateMachine.State.GameInitialisation<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderType, ConnectionType>, WordsProviderType, *> -> {
                    val player = previousState.metadata.allPlayersList.firstThatOrNull { it.metadata.id == playerID }
                    
                    if (player == null) {
                        val metadata = initialMetadataFactory(playerID)
                        if (checkConnectionAttachment(metadata, false, connection)) {
                            val newPlayer = Player(this, metadata)
                            previousState.metadata.allPlayersList.add(newPlayer)
                            val node = newPlayer.connectionsRegistry.addNode(connection)
                            val attachment = Player.Attachment(newPlayer, node)
                            
                            TransitionOrReasonAndComputation.Success(
                                GameStateMachine.Transition.UpdateGame.UpdateGameSettings(
                                    playersList = previousState.metadata.allPlayersList.filter { it.isOnline },
                                    settingsBuilder = previousState.settingsBuilder,
                                ),
                                attachment,
                            )
                        } else {
                            connection.sendError(Outgoing.Error.AttachmentIsDenied)
                            
                            TransitionOrReasonAndComputation.Failure(null, null)
                        }
                    } else {
                        if (checkConnectionAttachment(player.metadata, player.isOnline, connection)) {
                            val node = player.connectionsRegistry.addNode(connection)
                            val attachment = Player.Attachment(player, node)
                            
                            TransitionOrReasonAndComputation.Success(
                                GameStateMachine.Transition.UpdateGame.UpdateGameSettings(
                                    playersList = previousState.metadata.allPlayersList.filter { it.isOnline },
                                    settingsBuilder = previousState.settingsBuilder,
                                ),
                                attachment,
                            )
                        } else {
                            connection.sendError(Outgoing.Error.AttachmentIsDenied)
                            
                            TransitionOrReasonAndComputation.Failure(null, null)
                        }
                    }
                }
                is GameStateMachine.State.RoundWaiting<*, *, *>,
                is GameStateMachine.State.RoundPreparation<*, *, *>,
                is GameStateMachine.State.RoundExplanation<*, *, *>,
                is GameStateMachine.State.RoundLastGuess<*, *, *>,
                is GameStateMachine.State.RoundEditing<*, *, *>,
                is GameStateMachine.State.GameResults<*, *>, -> {
                    val player = previousState.playersList.firstThatOrNull { it.metadata.id == playerID }
                    if (player == null || !checkConnectionAttachment(player.metadata, player.isOnline, connection)) {
                        connection.sendError(Outgoing.Error.AttachmentIsDenied)
                        
                        TransitionOrReasonAndComputation.Failure(null, null)
                    } else {
                        val node = player.connectionsRegistry.addNode(connection)
                        val attachment = Player.Attachment(player, node)
                        
                        TransitionOrReasonAndComputation.Success(
                            GameStateMachine.Transition.UpdateMetadata(
                                AttachConnection
                            ),
                            attachment,
                        )
                    }
                }
            }
        }.computation
}