package dev.lounres.halfhat.logic.server

import dev.lounres.halfhat.logic.gameStateMachine.*
import dev.lounres.kone.algebraic.order
import dev.lounres.kone.automata.*
import dev.lounres.kone.collections.array.KoneUIntArray
import dev.lounres.kone.collections.interop.toKoneUIntArray
import dev.lounres.kone.collections.iterables.isEmpty
import dev.lounres.kone.collections.iterables.isNotEmpty
import dev.lounres.kone.collections.list.*
import dev.lounres.kone.collections.list.implementations.KoneGCLinkedSizedList
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.utils.*
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.relations.ComparisonResult
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Order
import dev.lounres.kone.relations.compareWith
import dev.lounres.kone.relations.defaultFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.random.Random
import kotlin.time.Duration


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
    initialSettingsBuilder: GameSettings.Builder<WordsProviderDescription>,
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
                player.room.gameStateMachine.moveMaybe { previousState ->
                    val isNodeActuallyAttached = !node.isDetached
                    
                    if (isNodeActuallyAttached) {
                        node.remove()
                        TransitionOrReason.Success(
                            when (previousState) {
                                is GameStateMachine.State.GameInitialisation<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, WordsProviderDescription, *> ->
                                    GameStateMachine.Transition.UpdateGameSettings(
                                        playersList = previousState.metadata.allPlayersList.filter { it.isOnline },
                                        settingsBuilder = previousState.settingsBuilder,
                                    )
                                
                                is GameStateMachine.State.PlayersWordsCollection<*, *, *>,
                                is GameStateMachine.State.RoundWaiting<*, *, *>,
                                is GameStateMachine.State.RoundPreparation<*, *, *>,
                                is GameStateMachine.State.RoundExplanation<*, *, *>,
                                is GameStateMachine.State.RoundLastGuess<*, *, *>,
                                is GameStateMachine.State.RoundEditing<*, *, *>,
                                is GameStateMachine.State.GameResults<*, *, *>,
                                    -> GameStateMachine.Transition.NoOperation()
                            }
                        )
                    } else {
                        node.element.sendError(Outgoing.Error.AttachmentIsAlreadySevered)
                        TransitionOrReason.Failure(null)
                    }
                }
            }
            
            public suspend fun updateGameSettings(settingsBuilderPatch: GameSettings.Builder.Patch<WordsProviderID>) {
                val result = player.room.gameStateMachine.moveMaybe { previousState ->
                    if (previousState.metadata.allPlayersList.firstThat { it.isOnline } != player) {
                        node.element.sendError(Outgoing.Error.NotHostChangingGameSettings)
                        return@moveMaybe TransitionOrReason.Failure(null)
                    }
                    when (previousState) {
                        is GameStateMachine.State.GameInitialisation<*, WordsProviderDescription, *> ->
                            TransitionOrReason.Success(
                                GameStateMachine.Transition.UpdateGameSettings(
                                    playersList = previousState.playersList,
                                    settingsBuilder = GameStateMachine.GameSettings.Builder(
                                        preparationTimeSeconds = settingsBuilderPatch.preparationTimeSeconds ?: previousState.settingsBuilder.preparationTimeSeconds,
                                        explanationTimeSeconds = settingsBuilderPatch.explanationTimeSeconds ?: previousState.settingsBuilder.explanationTimeSeconds,
                                        finalGuessTimeSeconds = settingsBuilderPatch.finalGuessTimeSeconds ?: previousState.settingsBuilder.finalGuessTimeSeconds,
                                        strictMode = settingsBuilderPatch.strictMode ?: previousState.settingsBuilder.strictMode,
                                        cachedEndConditionWordsNumber = settingsBuilderPatch.cachedEndConditionWordsNumber ?: previousState.settingsBuilder.cachedEndConditionWordsNumber,
                                        cachedEndConditionCyclesNumber = settingsBuilderPatch.cachedEndConditionCyclesNumber ?: previousState.settingsBuilder.cachedEndConditionCyclesNumber,
                                        gameEndConditionType = settingsBuilderPatch.gameEndConditionType ?: previousState.settingsBuilder.gameEndConditionType,
                                        wordsSource = when (val wordsSource = settingsBuilderPatch.wordsSource) {
                                            null -> previousState.settingsBuilder.wordsSource
                                            WordsSource.Players -> GameStateMachine.WordsSource.Players
                                            is WordsSource.Custom<WordsProviderID> -> when (val wordsProviderId = player.room.wordsProviderRegistry.getWordsProviderDescription(wordsSource.description)) {
                                                is WordsProviderRegistry.WordsProviderDescriptionOrReason.Failure<NoWordsProviderReason> -> return@moveMaybe TransitionOrReason.Failure(null)
                                                is WordsProviderRegistry.WordsProviderDescriptionOrReason.Success -> GameStateMachine.WordsSource.Custom(wordsProviderId.result)
                                            }
                                        },
                                    ),
                                )
                            )
                        is GameStateMachine.State.PlayersWordsCollection<*, *, *>,
                        is GameStateMachine.State.RoundWaiting<*, *, *>,
                        is GameStateMachine.State.RoundPreparation<*, *, *>,
                        is GameStateMachine.State.RoundExplanation<*, *, *>,
                        is GameStateMachine.State.RoundLastGuess<*, *, *>,
                        is GameStateMachine.State.RoundEditing<*, *, *>,
                        is GameStateMachine.State.GameResults<*, *, *>,
                            -> TransitionOrReason.Failure(null)
                    }
                }
                when (result) {
                    is MovementMaybeResult.NoTransition<*, *> -> {}
                    is MovementMaybeResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?, NoWordsProviderReason>> -> {
                        val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                        if (error != null) node.element.sendError(error)
                    }
                    is MovementMaybeResult.Success<*, *> -> {}
                }
            }
            
            public suspend fun initializeGame() {
                val result = player.room.gameStateMachine.moveMaybe { previousState ->
                    if (previousState.metadata.allPlayersList.firstThat { it.isOnline } != player) {
                        node.element.sendError(Outgoing.Error.NotHostChangingGameSettings)
                        return@moveMaybe TransitionOrReason.Failure(null)
                    }
                    TransitionOrReason.Success(
                        GameStateMachine.Transition.InitialiseGame(
                            wordsProviderRegistry = object : GameStateMachine.WordsProviderRegistry<WordsProviderDescription, NoWordsProviderReason> {
                                override suspend fun getWordsProvider(providerId: WordsProviderDescription): GameStateMachine.WordsProviderRegistry.WordsProviderOrReason<NoWordsProviderReason> =
                                    player.room.wordsProviderRegistry.getWordsProvider(providerId.providerId)
                            },
                        )
                    )
                }
                when (result) {
                    is MovementMaybeResult.NoTransition<*, *> -> {}
                    is MovementMaybeResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?, NoWordsProviderReason>> -> {
                        val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                        if (error != null) node.element.sendError(error)
                    }
                    is MovementMaybeResult.Success<*, *> -> {}
                }
            }
            
            public suspend fun submitWords(words: KoneSet<String>) {
                val result = player.room.gameStateMachine.move { previousState ->
                    GameStateMachine.Transition.SubmitPlayerWords(
                        playerIndex = (Equality.defaultFor<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>>()) { previousState.playersList.firstIndexOf(player) },
                        playerWords = words,
                    )
                }
                when (result) {
                    is MovementResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?, NoWordsProviderReason>> -> {
                        val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                        if (error != null) node.element.sendError(error)
                    }
                    is MovementResult.Success<*, *> -> {}
                }
            }
            
            public suspend fun speakerReady() {
                val result = player.room.gameStateMachine.moveMaybe { previousState ->
                    when (previousState) {
                        is GameStateMachine.State.RoundWaiting<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, *, *> ->
                            if (previousState.speaker != player) {
                                node.element.sendError(Outgoing.Error.NotSpeakerSettingSpeakerReadiness)
                                TransitionOrReason.Failure(null)
                            } else TransitionOrReason.Success(GameStateMachine.Transition.SpeakerReady())
                        is GameStateMachine.State.GameInitialisation<*, *, *>,
                        is GameStateMachine.State.PlayersWordsCollection<*, *, *>,
                        is GameStateMachine.State.RoundPreparation<*, *, *>,
                        is GameStateMachine.State.RoundExplanation<*, *, *>,
                        is GameStateMachine.State.RoundLastGuess<*, *, *>,
                        is GameStateMachine.State.RoundEditing<*, *, *>,
                        is GameStateMachine.State.GameResults<*, *, *>,
                            -> TransitionOrReason.Failure(null)
                    }
                }
                when (result) {
                    is MovementMaybeResult.NoTransition<*, *> -> {}
                    is MovementMaybeResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?, NoWordsProviderReason>> -> {
                        val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                        if (error != null) node.element.sendError(error)
                    }
                    is MovementMaybeResult.Success<*, *> -> {}
                }
            }
            
            public suspend fun listenerReady() {
                val result = player.room.gameStateMachine.moveMaybe { previousState ->
                    when (previousState) {
                        is GameStateMachine.State.RoundWaiting<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, *, *> ->
                            if (previousState.listener != player) {
                                node.element.sendError(Outgoing.Error.NotListenerSettingListenerReadiness)
                                TransitionOrReason.Failure(null)
                            } else TransitionOrReason.Success(GameStateMachine.Transition.ListenerReady())
                        is GameStateMachine.State.GameInitialisation<*, *, *>,
                        is GameStateMachine.State.PlayersWordsCollection<*, *, *>,
                        is GameStateMachine.State.RoundPreparation<*, *, *>,
                        is GameStateMachine.State.RoundExplanation<*, *, *>,
                        is GameStateMachine.State.RoundLastGuess<*, *, *>,
                        is GameStateMachine.State.RoundEditing<*, *, *>,
                        is GameStateMachine.State.GameResults<*, *, *>,
                            -> TransitionOrReason.Failure(null)
                    }
                }
                when (result) {
                    is MovementMaybeResult.NoTransition<*, *> -> {}
                    is MovementMaybeResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?, NoWordsProviderReason>> -> {
                        val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                        if (error != null) node.element.sendError(error)
                    }
                    is MovementMaybeResult.Success<*, *> -> {}
                }
            }
            
            public suspend fun wordExplanationState(state: GameStateMachine.WordExplanation.State) {
                val result = player.room.gameStateMachine.moveMaybe { previousState ->
                    when (previousState) {
                        is GameStateMachine.State.RoundExplanation<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, *, *> ->
                            if (previousState.speaker != player) {
                                node.element.sendError(Outgoing.Error.NotSpeakerSubmittingWordExplanationResult)
                                TransitionOrReason.Failure(null)
                            } else TransitionOrReason.Success(GameStateMachine.Transition.WordExplanationState(state))
                        is GameStateMachine.State.RoundLastGuess<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, *, *> ->
                            if (previousState.speaker != player) {
                                node.element.sendError(Outgoing.Error.NotSpeakerSubmittingWordExplanationResult)
                                TransitionOrReason.Failure(null)
                            } else TransitionOrReason.Success(GameStateMachine.Transition.WordExplanationState(state))
                        is GameStateMachine.State.GameInitialisation<*, *, *>,
                        is GameStateMachine.State.PlayersWordsCollection<*, *, *>,
                        is GameStateMachine.State.RoundWaiting<*, *, *>,
                        is GameStateMachine.State.RoundPreparation<*, *, *>,
                        is GameStateMachine.State.RoundEditing<*, *, *>,
                        is GameStateMachine.State.GameResults<*, *, *>,
                            -> TransitionOrReason.Failure(null)
                    }
                }
                when (result) {
                    is MovementMaybeResult.NoTransition<*, *> -> {}
                    is MovementMaybeResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?, NoWordsProviderReason>> -> {
                        val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                        if (error != null) node.element.sendError(error)
                    }
                    is MovementMaybeResult.Success<*, *> -> {}
                }
            }
            
            public suspend fun updateWordsExplanationResults(newExplanationResults: KoneList<GameStateMachine.WordExplanation>) {
                val result = player.room.gameStateMachine.moveMaybe { previousState ->
                    when (previousState) {
                        is GameStateMachine.State.RoundEditing<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, *, *> ->
                            if (previousState.speaker != player) {
                                node.element.sendError(Outgoing.Error.NotSpeakerUpdatingWordExplanationResults)
                                TransitionOrReason.Failure(null)
                            } else TransitionOrReason.Success(GameStateMachine.Transition.UpdateWordsExplanationResults(newExplanationResults))
                        is GameStateMachine.State.GameInitialisation<*, *, *>,
                        is GameStateMachine.State.PlayersWordsCollection<*, *, *>,
                        is GameStateMachine.State.RoundWaiting<*, *, *>,
                        is GameStateMachine.State.RoundPreparation<*, *, *>,
                        is GameStateMachine.State.RoundExplanation<*, *, *>,
                        is GameStateMachine.State.RoundLastGuess<*, *, *>,
                        is GameStateMachine.State.GameResults<*, *, *>,
                            -> TransitionOrReason.Failure(null)
                    }
                }
                when (result) {
                    is MovementMaybeResult.NoTransition<*, *> -> {}
                    is MovementMaybeResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?, NoWordsProviderReason>> -> {
                        val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                        if (error != null) node.element.sendError(error)
                    }
                    is MovementMaybeResult.Success<*, *> -> {}
                }
            }
            
            public suspend fun confirmWordsExplanationResults() {
                val result = player.room.gameStateMachine.moveMaybe { previousState ->
                    when (previousState) {
                        is GameStateMachine.State.RoundEditing<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, *, *> ->
                            if (previousState.speaker != player) {
                                node.element.sendError(Outgoing.Error.NotSpeakerConfirmingWordExplanationResults)
                                TransitionOrReason.Failure(null)
                            } else TransitionOrReason.Success(GameStateMachine.Transition.ConfirmWordsExplanationResults())
                        is GameStateMachine.State.GameInitialisation<*, *, *>,
                        is GameStateMachine.State.PlayersWordsCollection<*, *, *>,
                        is GameStateMachine.State.RoundWaiting<*, *, *>,
                        is GameStateMachine.State.RoundPreparation<*, *, *>,
                        is GameStateMachine.State.RoundExplanation<*, *, *>,
                        is GameStateMachine.State.RoundLastGuess<*, *, *>,
                        is GameStateMachine.State.GameResults<*, *, *>,
                            -> TransitionOrReason.Failure(null)
                    }
                }
                when (result) {
                    is MovementMaybeResult.NoTransition<*, *> -> {}
                    is MovementMaybeResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?, NoWordsProviderReason>> -> {
                        val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                        if (error != null) node.element.sendError(error)
                    }
                    is MovementMaybeResult.Success<*, *> -> {}
                }
            }
            
            public suspend fun finishGame() {
                val result = player.room.gameStateMachine.moveMaybe { previousState ->
                    if (previousState.metadata.allPlayersList.firstThat { it.isOnline } != player) {
                        node.element.sendError(Outgoing.Error.NotHostFinishingGame)
                        return@moveMaybe TransitionOrReason.Failure(null)
                    }
                    TransitionOrReason.Success(
                        GameStateMachine.Transition.FinishGame()
                    )
                }
                when (result) {
                    is MovementMaybeResult.NoTransition<*, *> -> {}
                    is MovementMaybeResult.NoNextState<*, *, GameStateMachine.NoNextStateReason<Nothing?, NoWordsProviderReason>> -> {
                        val error = Outgoing.Error.fromGameStateMachineNoNextStateReason(result.noNextStateReason)
                        if (error != null) node.element.sendError(error)
                    }
                    is MovementMaybeResult.Success<*, *> -> {}
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
    }
    
    public data class Description<out RoomMetadata, out PlayerMetadata>(
        val metadata: RoomMetadata,
        val playersList: KoneList<Player.Description<PlayerMetadata>>,
        val stateType: StateType
    )
    
    public enum class StateType {
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
        public data class Custom<WordsProviderDescription>(
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
    
    public object Outgoing {
        public sealed interface PlayerDescription<out PlayerMetadata> {
            public val metadata: PlayerMetadata
            public val userIndex: UInt
            public val isOnline: Boolean
            public val isHost: Boolean
            
            public data class GameInitialisation<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isOnline: Boolean,
                override val isHost: Boolean,
            ) : PlayerDescription<PlayerMetadata>
            
            public data class PlayersWordsCollection<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isOnline: Boolean,
                override val isHost: Boolean,
                public val finishedWordsCollection: Boolean,
            ) : PlayerDescription<PlayerMetadata>
            
            public sealed interface Round<out PlayerMetadata> : PlayerDescription<PlayerMetadata> {
                public val roundRole: RoundRole
                public val scoreExplained: UInt
                public val scoreGuessed: UInt
                public val scoreSum: UInt
                
                public enum class RoundRole {
                    Player, Speaker, Listener,
                }
                
                public data class Waiting<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val roundRole: RoundRole,
                    override val scoreExplained: UInt,
                    override val scoreGuessed: UInt,
                    override val scoreSum: UInt,
                ) : Round<PlayerMetadata>
                
                public data class Preparation<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val roundRole: RoundRole,
                    override val scoreExplained: UInt,
                    override val scoreGuessed: UInt,
                    override val scoreSum: UInt,
                ) : Round<PlayerMetadata>
                
                public data class Explanation<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val roundRole: RoundRole,
                    override val scoreExplained: UInt,
                    override val scoreGuessed: UInt,
                    override val scoreSum: UInt,
                ) : Round<PlayerMetadata>
                
                public data class LastGuess<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val roundRole: RoundRole,
                    override val scoreExplained: UInt,
                    override val scoreGuessed: UInt,
                    override val scoreSum: UInt,
                ) : Round<PlayerMetadata>
                
                public data class Editing<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val roundRole: RoundRole,
                    override val scoreExplained: UInt,
                    override val scoreGuessed: UInt,
                    override val scoreSum: UInt,
                ) : Round<PlayerMetadata>
            }
            
            public data class GameResults<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isOnline: Boolean,
                override val isHost: Boolean,
                public val scoreExplained: UInt,
                public val scoreGuessed: UInt,
                public val scoreSum: UInt,
            ) : PlayerDescription<PlayerMetadata>
        }
        
        public sealed interface Role<out PlayerMetadata> {
            public val metadata: PlayerMetadata
            public val userIndex: UInt
            public val isHost: Boolean
            
            public data class GameInitialisation<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val isStartAvailable: Boolean,
                public val areSettingsChangeable: Boolean,
            ) : Role<PlayerMetadata>
            
            public data class PlayersWordsCollection<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val finishedWordsCollection: Boolean,
            ) : Role<PlayerMetadata>
            
            public sealed interface Round<out PlayerMetadata> : Role<PlayerMetadata> {
                public val roundsBeforeSpeaking: UInt
                public val roundsBeforeListening: UInt
                
                public data class Waiting<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val roundRole: RoundRole,
                    public val isGameFinishable: Boolean,
                    override val roundsBeforeSpeaking: UInt,
                    override val roundsBeforeListening: UInt,
                ) : Round<PlayerMetadata> {
                    public enum class RoundRole {
                        Player, Speaker, Listener,
                    }
                }
                
                public data class Preparation<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val roundRole: RoundRole,
                    override val roundsBeforeSpeaking: UInt,
                    override val roundsBeforeListening: UInt,
                ) : Round<PlayerMetadata> {
                    public enum class RoundRole {
                        Player, Speaker, Listener,
                    }
                }
                
                public data class Explanation<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val roundRole: RoundRole,
                    override val roundsBeforeSpeaking: UInt,
                    override val roundsBeforeListening: UInt,
                ) : Round<PlayerMetadata> {
                    public sealed interface RoundRole {
                        public data object Player : RoundRole
                        public data class Speaker(val currentWord: String) : RoundRole
                        public data object Listener : RoundRole
                    }
                }
                
                public data class LastGuess<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val roundRole: RoundRole,
                    override val roundsBeforeSpeaking: UInt,
                    override val roundsBeforeListening: UInt,
                ) : Round<PlayerMetadata> {
                    public sealed interface RoundRole {
                        public data object Player : RoundRole
                        public data class Speaker(val currentWord: String) : RoundRole
                        public data object Listener : RoundRole
                    }
                }
                
                public data class Editing<out PlayerMetadata>(
                    override val metadata: PlayerMetadata,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val roundRole: RoundRole,
                    override val roundsBeforeSpeaking: UInt,
                    override val roundsBeforeListening: UInt,
                ) : Round<PlayerMetadata> {
                    public sealed interface RoundRole {
                        public data object Player : RoundRole
                        public data class Speaker(public val wordsToEdit: KoneList<GameStateMachine.WordExplanation>) : RoundRole
                        public data object Listener : RoundRole
                    }
                }
            }
            
            public data class GameResults<out PlayerMetadata>(
                override val metadata: PlayerMetadata,
                override val userIndex: UInt,
                override val isHost: Boolean,
            ) : Role<PlayerMetadata>
        }
        
        public sealed interface State<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription> {
            public val roomMetadata: RoomMetadata
            public val role: Role<PlayerMetadata>
            
            public data class GameInitialisation<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                override val roomMetadata: RoomMetadata,
                override val role: Role.GameInitialisation<PlayerMetadata>,
                public val playersList: KoneList<PlayerDescription.GameInitialisation<PlayerMetadata>>,
                public val settingsBuilder: GameSettings.Builder<WordsProviderDescription>,
            ) : State<RoomMetadata, PlayerMetadata, WordsProviderDescription>
            
            public data class PlayersWordsCollection<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                override val roomMetadata: RoomMetadata,
                override val role: Role.PlayersWordsCollection<PlayerMetadata>,
                public val playersList: KoneList<PlayerDescription.PlayersWordsCollection<PlayerMetadata>>,
                public val settings: GameSettings<WordsProviderDescription>,
            ) : State<RoomMetadata, PlayerMetadata, WordsProviderDescription>
            
            public sealed interface Round<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription> : State<RoomMetadata, PlayerMetadata, WordsProviderDescription> {
                override val role: Role.Round<PlayerMetadata>
                public val playersList: KoneList<PlayerDescription.Round<PlayerMetadata>>
                public val settings: GameSettings<WordsProviderDescription>
                public val initialWordsNumber: UInt
                public val roundNumber: UInt
                public val cycleNumber: UInt
                public val speakerIndex: UInt
                public val listenerIndex: UInt
                public val nextSpeakerIndex: UInt
                public val nextListenerIndex: UInt
                public val restWordsNumber: UInt
                public val wordsInProgressNumber: UInt
                public val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>
                public val leaderboardPermutation: KoneUIntArray
                
                public data class Waiting<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                    override val roomMetadata: RoomMetadata,
                    override val role: Role.Round.Waiting<PlayerMetadata>,
                    override val playersList: KoneList<PlayerDescription.Round.Waiting<PlayerMetadata>>,
                    override val settings: GameSettings<WordsProviderDescription>,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>,
                    public val speakerReady: Boolean,
                    public val listenerReady: Boolean,
                    override val leaderboardPermutation: KoneUIntArray,
                ) : Round<RoomMetadata, PlayerMetadata, WordsProviderDescription>
                
                public data class Preparation<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                    override val roomMetadata: RoomMetadata,
                    override val role: Role.Round.Preparation<PlayerMetadata>,
                    override val playersList: KoneList<PlayerDescription.Round.Preparation<PlayerMetadata>>,
                    override val settings: GameSettings<WordsProviderDescription>,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>,
                    public val millisecondsLeft: UInt,
                    override val leaderboardPermutation: KoneUIntArray,
                ) : Round<RoomMetadata, PlayerMetadata, WordsProviderDescription>
                
                public data class Explanation<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                    override val roomMetadata: RoomMetadata,
                    override val role: Role.Round.Explanation<PlayerMetadata>,
                    override val playersList: KoneList<PlayerDescription.Round.Explanation<PlayerMetadata>>,
                    override val settings: GameSettings<WordsProviderDescription>,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>,
                    public val millisecondsLeft: UInt,
                    override val leaderboardPermutation: KoneUIntArray,
                ) : Round<RoomMetadata, PlayerMetadata, WordsProviderDescription>
                
                public data class LastGuess<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                    override val roomMetadata: RoomMetadata,
                    override val role: Role.Round.LastGuess<PlayerMetadata>,
                    override val playersList: KoneList<PlayerDescription.Round.LastGuess<PlayerMetadata>>,
                    override val settings: GameSettings<WordsProviderDescription>,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>,
                    public val millisecondsLeft: UInt,
                    override val leaderboardPermutation: KoneUIntArray,
                ) : Round<RoomMetadata, PlayerMetadata, WordsProviderDescription>
                
                public data class Editing<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                    override val roomMetadata: RoomMetadata,
                    override val role: Role.Round.Editing<PlayerMetadata>,
                    override val playersList: KoneList<PlayerDescription.Round.Editing<PlayerMetadata>>,
                    override val settings: GameSettings<WordsProviderDescription>,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>,
                    override val leaderboardPermutation: KoneUIntArray,
                ) : Round<RoomMetadata, PlayerMetadata, WordsProviderDescription>
            }
            
            public data class GameResults<out RoomMetadata, out PlayerMetadata, out WordsProviderDescription>(
                override val roomMetadata: RoomMetadata,
                override val role: Role.GameResults<PlayerMetadata>,
                public val playersList: KoneList<PlayerDescription.GameResults<PlayerMetadata>>,
                public val settings: GameSettings<WordsProviderDescription>,
                public val leaderboardPermutation: KoneUIntArray,
                public val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>,
            ) : State<RoomMetadata, PlayerMetadata, WordsProviderDescription>
        }
        
        public sealed interface Error<out NoWordsProviderReason> {
            public data object AttachmentIsDenied : Error<Nothing>
            public data object AttachmentIsAlreadySevered : Error<Nothing>
            public data object NotHostChangingGameSettings : Error<Nothing>
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
            public data object CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting : Error<Nothing>
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
                internal fun <NoWordsProviderReason> fromGameStateMachineNoNextStateReason(reason: GameStateMachine.NoNextStateReason<Nothing?, NoWordsProviderReason>): Error<NoWordsProviderReason>? =
                    when (reason) {
                        is GameStateMachine.NoNextStateReason.NoMetadataUpdate<*> -> null
                        GameStateMachine.NoNextStateReason.CannotUpdateGameSettingsAfterInitialization -> CannotUpdateGameSettingsAfterInitialization
                        GameStateMachine.NoNextStateReason.NotEnoughPlayersForInitialization -> NotEnoughPlayersForInitialization
                        is GameStateMachine.NoNextStateReason.NoWordsProvider<NoWordsProviderReason> -> NoWordsProvider(reason.reason)
                        GameStateMachine.NoNextStateReason.CannotInitializeGameAfterInitialization -> CannotInitializeGameAfterInitialization
                        GameStateMachine.NoNextStateReason.PlayerAlreadySubmittedWords -> PlayerAlreadySubmittedWords
                        GameStateMachine.NoNextStateReason.CannotSubmitPlayerWordsNotDuringPlayersWordsCollection -> CannotSubmitPlayerWordsNotDuringPlayersWordsCollection
                        GameStateMachine.NoNextStateReason.CannotSetSpeakerReadinessNotDuringRoundWaiting -> CannotSetSpeakerReadinessNotDuringRoundWaiting
                        GameStateMachine.NoNextStateReason.CannotSetListenerReadinessNotDuringRoundWaiting -> CannotSetListenerReadinessNotDuringRoundWaiting
                        GameStateMachine.NoNextStateReason.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting -> CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting
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
    
    private data class GameStateMachineMetadata<RoomMetadata, PlayerID, PlayerMetadata : Player.Metadata<PlayerID>, WordsProviderID, WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>, NoWordsProviderReason, ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>>(
        val allPlayersList: KoneList<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>>,
    )
    
    private sealed interface GameStateMachineMetadataTransition<RoomMetadata, PlayerID, PlayerMetadata : Player.Metadata<PlayerID>, WordsProviderID, WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>, NoWordsProviderReason, ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>> {
        data class UpdateAllPlayersList<RoomMetadata, PlayerID, PlayerMetadata : Player.Metadata<PlayerID>, WordsProviderID, WordsProviderDescription : Room.WordsProviderDescription<WordsProviderID>, NoWordsProviderReason, ConnectionType: Connection<RoomMetadata, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason>>(
            val newAllPlayersList: KoneList<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>>,
        ) : GameStateMachineMetadataTransition<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>
    }
    
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private val random: Random = Random
    
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
    
    private val gameStateMachine =
        AsynchronousGameStateMachine.Initialization<_, WordsProviderDescription, NoWordsProviderReason, _, _, _>(
            coroutineScope = coroutineScope,
            random = random,
            metadata = GameStateMachineMetadata<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>(
                allPlayersList = KoneGCLinkedSizedList(),
            ),
            playersList = KoneList.empty<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>>(),
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
                    is WordsSource.Custom<WordsProviderDescription> -> GameStateMachine.WordsSource.Custom(source.description)
                },
            ),
            checkMetadataUpdate = { previousState, metadataTransition: GameStateMachineMetadataTransition<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType> ->
                when (metadataTransition) {
                    is GameStateMachineMetadataTransition.UpdateAllPlayersList -> {
                        when (previousState) {
                            is GameStateMachine.State.GameInitialisation<*, *, *> ->
                                CheckResult.Success(
                                    GameStateMachineMetadata(
                                        allPlayersList = metadataTransition.newAllPlayersList,
                                    )
                                )
                            is GameStateMachine.State.PlayersWordsCollection<*, *, *>,
                            is GameStateMachine.State.RoundWaiting<*, *, *>,
                            is GameStateMachine.State.RoundPreparation<*, *, *>,
                            is GameStateMachine.State.RoundExplanation<*, *, *>,
                            is GameStateMachine.State.RoundLastGuess<*, *, *>,
                            is GameStateMachine.State.RoundEditing<*, *, *>,
                            is GameStateMachine.State.GameResults<*, *, *>,
                                -> CheckResult.Failure(null)
                        }
                    }
                }
            }
        ) { _, _, nextState ->
            val hostIndex = nextState.playersList.firstIndexThat { _, player -> player.isOnline }
            val playersList = nextState.playersList.mapIndexed { index, player ->
                Player.Description(metadata = player.metadata, isOnline = player.isOnline, isHost = index == hostIndex)
            }
            nextState.playersList.forEachIndexed { index, player ->
                val gameStateToSend = when (nextState) {
                    is GameStateMachine.State.GameInitialisation<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, WordsProviderDescription, *> -> {
                        val gameMachineSettingsBuilder = nextState.settingsBuilder
                        Outgoing.State.GameInitialisation(
                            roomMetadata = metadata,
                            role = Outgoing.Role.GameInitialisation(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                                isStartAvailable = index == hostIndex && nextState.playersList.size >= 2u,
                                areSettingsChangeable = index == hostIndex,
                            ),
                            playersList = playersList.mapIndexed { index, player ->
                                Outgoing.PlayerDescription.GameInitialisation(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                )
                            },
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
                                    is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                                }
                            )
                        )
                    }
                    is GameStateMachine.State.PlayersWordsCollection<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, WordsProviderDescription, *> -> {
                        val gameMachineSettings = nextState.settings
                        Outgoing.State.PlayersWordsCollection(
                            roomMetadata = metadata,
                            role = Outgoing.Role.PlayersWordsCollection(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                                finishedWordsCollection = nextState.playersWords[index] != null,
                            ),
                            playersList = playersList.mapIndexed { index, player ->
                                Outgoing.PlayerDescription.PlayersWordsCollection(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                    finishedWordsCollection = nextState.playersWords[index] != null,
                                )
                            },
                            settings = GameSettings(
                                preparationTimeSeconds = gameMachineSettings.preparationTimeSeconds,
                                explanationTimeSeconds = gameMachineSettings.explanationTimeSeconds,
                                finalGuessTimeSeconds = gameMachineSettings.finalGuessTimeSeconds,
                                strictMode = gameMachineSettings.strictMode,
                                gameEndCondition = gameMachineSettings.gameEndCondition,
                                wordsSource = when (val wordsSource = gameMachineSettings.wordsSource) {
                                    GameStateMachine.WordsSource.Players -> WordsSource.Players
                                    is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                                },
                            ),
                        )
                    }
                    is GameStateMachine.State.RoundWaiting<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, WordsProviderDescription, *> -> {
                        val gameMachineSettings = nextState.settings
                        Outgoing.State.Round.Waiting(
                            roomMetadata = metadata,
                            role = Outgoing.Role.Round.Waiting(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                                roundRole = when (index) {
                                    nextState.speakerIndex -> Outgoing.Role.Round.Waiting.RoundRole.Speaker
                                    nextState.listenerIndex -> Outgoing.Role.Round.Waiting.RoundRole.Listener
                                    else -> Outgoing.Role.Round.Waiting.RoundRole.Player
                                },
                                isGameFinishable = index == hostIndex,
                                roundsBeforeSpeaking = nextState.playersRoundsBeforeSpeaking[index],
                                roundsBeforeListening = nextState.playersRoundsBeforeListening[index],
                            ),
                            playersList = playersList.mapIndexed { index, player ->
                                Outgoing.PlayerDescription.Round.Waiting(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                    roundRole = when (index) {
                                        nextState.speakerIndex -> Outgoing.PlayerDescription.Round.RoundRole.Speaker
                                        nextState.listenerIndex -> Outgoing.PlayerDescription.Round.RoundRole.Listener
                                        else -> Outgoing.PlayerDescription.Round.RoundRole.Player
                                    },
                                    scoreExplained = nextState.explanationScores[index],
                                    scoreGuessed = nextState.guessingScores[index],
                                    scoreSum = nextState.explanationScores[index] + nextState.guessingScores[index],
                                )
                            },
                            settings = GameSettings(
                                preparationTimeSeconds = gameMachineSettings.preparationTimeSeconds,
                                explanationTimeSeconds = gameMachineSettings.explanationTimeSeconds,
                                finalGuessTimeSeconds = gameMachineSettings.finalGuessTimeSeconds,
                                strictMode = gameMachineSettings.strictMode,
                                gameEndCondition = gameMachineSettings.gameEndCondition,
                                wordsSource = when (val wordsSource = gameMachineSettings.wordsSource) {
                                    GameStateMachine.WordsSource.Players -> WordsSource.Players
                                    is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                                },
                            ),
                            initialWordsNumber = nextState.initialWordsNumber,
                            roundNumber =  nextState.roundNumber,
                            cycleNumber = nextState.cycleNumber,
                            speakerIndex = nextState.speakerIndex,
                            listenerIndex = nextState.listenerIndex,
                            nextSpeakerIndex = nextState.nextSpeakerIndex,
                            nextListenerIndex = nextState.nextListenerIndex,
                            restWordsNumber = nextState.restWords.size,
                            wordsInProgressNumber = 0u,
                            wordsStatistic = nextState.wordsStatistic.toOutgoingAPI().filter { it.state != GameStateMachine.WordStatistic.State.InProgress },
                            speakerReady = nextState.speakerReady,
                            listenerReady = nextState.listenerReady,
                            leaderboardPermutation = playersList
                                .indices
                                .sortedByDescending { nextState.explanationScores[it] + nextState.guessingScores[it] }
                                .toKoneUIntArray(),
                        )
                    }
                    is GameStateMachine.State.RoundPreparation<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, WordsProviderDescription, *> -> {
                        val gameMachineSettings = nextState.settings
                        Outgoing.State.Round.Preparation(
                            roomMetadata = metadata,
                            role = Outgoing.Role.Round.Preparation(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                                roundRole = when (index) {
                                    nextState.speakerIndex -> Outgoing.Role.Round.Preparation.RoundRole.Speaker
                                    nextState.listenerIndex -> Outgoing.Role.Round.Preparation.RoundRole.Listener
                                    else -> Outgoing.Role.Round.Preparation.RoundRole.Player
                                },
                                roundsBeforeSpeaking = nextState.playersRoundsBeforeSpeaking[index],
                                roundsBeforeListening = nextState.playersRoundsBeforeListening[index],
                            ),
                            playersList = playersList.mapIndexed { index, player ->
                                Outgoing.PlayerDescription.Round.Preparation(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                    roundRole = when (index) {
                                        nextState.speakerIndex -> Outgoing.PlayerDescription.Round.RoundRole.Speaker
                                        nextState.listenerIndex -> Outgoing.PlayerDescription.Round.RoundRole.Listener
                                        else -> Outgoing.PlayerDescription.Round.RoundRole.Player
                                    },
                                    scoreExplained = nextState.explanationScores[index],
                                    scoreGuessed = nextState.guessingScores[index],
                                    scoreSum = nextState.explanationScores[index] + nextState.guessingScores[index],
                                )
                            },
                            settings = GameSettings(
                                preparationTimeSeconds = gameMachineSettings.preparationTimeSeconds,
                                explanationTimeSeconds = gameMachineSettings.explanationTimeSeconds,
                                finalGuessTimeSeconds = gameMachineSettings.finalGuessTimeSeconds,
                                strictMode = gameMachineSettings.strictMode,
                                gameEndCondition = gameMachineSettings.gameEndCondition,
                                wordsSource = when (val wordsSource = gameMachineSettings.wordsSource) {
                                    GameStateMachine.WordsSource.Players -> WordsSource.Players
                                    is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                                },
                            ),
                            initialWordsNumber = nextState.initialWordsNumber,
                            roundNumber =  nextState.roundNumber,
                            cycleNumber = nextState.cycleNumber,
                            speakerIndex = nextState.speakerIndex,
                            listenerIndex = nextState.listenerIndex,
                            nextSpeakerIndex = nextState.nextSpeakerIndex,
                            nextListenerIndex = nextState.nextListenerIndex,
                            restWordsNumber = nextState.restWords.size,
                            wordsInProgressNumber = nextState.currentExplanationResults.size,
                            wordsStatistic = nextState.wordsStatistic.toOutgoingAPI().filter { it.state != GameStateMachine.WordStatistic.State.InProgress },
                            millisecondsLeft = nextState.millisecondsLeft,
                            leaderboardPermutation = playersList
                                .indices
                                .sortedByDescending { nextState.explanationScores[it] + nextState.guessingScores[it] }
                                .toKoneUIntArray(),
                        )
                    }
                    is GameStateMachine.State.RoundExplanation<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, WordsProviderDescription, *> -> {
                        val gameMachineSettings = nextState.settings
                        Outgoing.State.Round.Explanation(
                            roomMetadata = metadata,
                            role = Outgoing.Role.Round.Explanation(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                                roundRole = when (index) {
                                    nextState.speakerIndex -> Outgoing.Role.Round.Explanation.RoundRole.Speaker(nextState.currentWord)
                                    nextState.listenerIndex -> Outgoing.Role.Round.Explanation.RoundRole.Listener
                                    else -> Outgoing.Role.Round.Explanation.RoundRole.Player
                                },
                                roundsBeforeSpeaking = nextState.playersRoundsBeforeSpeaking[index],
                                roundsBeforeListening = nextState.playersRoundsBeforeListening[index],
                            ),
                            playersList = playersList.mapIndexed { index, player ->
                                Outgoing.PlayerDescription.Round.Explanation(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                    roundRole = when (index) {
                                        nextState.speakerIndex -> Outgoing.PlayerDescription.Round.RoundRole.Speaker
                                        nextState.listenerIndex -> Outgoing.PlayerDescription.Round.RoundRole.Listener
                                        else -> Outgoing.PlayerDescription.Round.RoundRole.Player
                                    },
                                    scoreExplained = nextState.explanationScores[index],
                                    scoreGuessed = nextState.guessingScores[index],
                                    scoreSum = nextState.explanationScores[index] + nextState.guessingScores[index],
                                )
                            },
                            settings = GameSettings(
                                preparationTimeSeconds = gameMachineSettings.preparationTimeSeconds,
                                explanationTimeSeconds = gameMachineSettings.explanationTimeSeconds,
                                finalGuessTimeSeconds = gameMachineSettings.finalGuessTimeSeconds,
                                strictMode = gameMachineSettings.strictMode,
                                gameEndCondition = gameMachineSettings.gameEndCondition,
                                wordsSource = when (val wordsSource = gameMachineSettings.wordsSource) {
                                    GameStateMachine.WordsSource.Players -> WordsSource.Players
                                    is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                                },
                            ),
                            initialWordsNumber = nextState.initialWordsNumber,
                            roundNumber =  nextState.roundNumber,
                            cycleNumber = nextState.cycleNumber,
                            speakerIndex = nextState.speakerIndex,
                            listenerIndex = nextState.listenerIndex,
                            nextSpeakerIndex = nextState.nextSpeakerIndex,
                            nextListenerIndex = nextState.nextListenerIndex,
                            restWordsNumber = nextState.restWords.size,
                            wordsInProgressNumber = nextState.currentExplanationResults.size + 1u,
                            wordsStatistic = nextState.wordsStatistic.toOutgoingAPI().filter { it.state != GameStateMachine.WordStatistic.State.InProgress },
                            millisecondsLeft = nextState.millisecondsLeft,
                            leaderboardPermutation = playersList
                                .indices
                                .sortedByDescending { nextState.explanationScores[it] + nextState.guessingScores[it] }
                                .toKoneUIntArray(),
                        )
                    }
                    is GameStateMachine.State.RoundLastGuess<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, WordsProviderDescription, *> -> {
                        val gameMachineSettings = nextState.settings
                        Outgoing.State.Round.LastGuess(
                            roomMetadata = metadata,
                            role = Outgoing.Role.Round.LastGuess(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                                roundRole = when (index) {
                                    nextState.speakerIndex -> Outgoing.Role.Round.LastGuess.RoundRole.Speaker(nextState.currentWord)
                                    nextState.listenerIndex -> Outgoing.Role.Round.LastGuess.RoundRole.Listener
                                    else -> Outgoing.Role.Round.LastGuess.RoundRole.Player
                                },
                                roundsBeforeSpeaking = nextState.playersRoundsBeforeSpeaking[index],
                                roundsBeforeListening = nextState.playersRoundsBeforeListening[index],
                            ),
                            playersList = playersList.mapIndexed { index, player ->
                                Outgoing.PlayerDescription.Round.LastGuess(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                    roundRole = when (index) {
                                        nextState.speakerIndex -> Outgoing.PlayerDescription.Round.RoundRole.Speaker
                                        nextState.listenerIndex -> Outgoing.PlayerDescription.Round.RoundRole.Listener
                                        else -> Outgoing.PlayerDescription.Round.RoundRole.Player
                                    },
                                    scoreExplained = nextState.explanationScores[index],
                                    scoreGuessed = nextState.guessingScores[index],
                                    scoreSum = nextState.explanationScores[index] + nextState.guessingScores[index],
                                )
                            },
                            settings = GameSettings(
                                preparationTimeSeconds = gameMachineSettings.preparationTimeSeconds,
                                explanationTimeSeconds = gameMachineSettings.explanationTimeSeconds,
                                finalGuessTimeSeconds = gameMachineSettings.finalGuessTimeSeconds,
                                strictMode = gameMachineSettings.strictMode,
                                gameEndCondition = gameMachineSettings.gameEndCondition,
                                wordsSource = when (val wordsSource = gameMachineSettings.wordsSource) {
                                    GameStateMachine.WordsSource.Players -> WordsSource.Players
                                    is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                                },
                            ),
                            initialWordsNumber = nextState.initialWordsNumber,
                            roundNumber =  nextState.roundNumber,
                            cycleNumber = nextState.cycleNumber,
                            speakerIndex = nextState.speakerIndex,
                            listenerIndex = nextState.listenerIndex,
                            nextSpeakerIndex = nextState.nextSpeakerIndex,
                            nextListenerIndex = nextState.nextListenerIndex,
                            restWordsNumber = nextState.restWords.size,
                            wordsInProgressNumber = nextState.currentExplanationResults.size + 1u,
                            wordsStatistic = nextState.wordsStatistic.toOutgoingAPI().filter { it.state != GameStateMachine.WordStatistic.State.InProgress },
                            millisecondsLeft = nextState.millisecondsLeft,
                            leaderboardPermutation = playersList
                                .indices
                                .sortedByDescending { nextState.explanationScores[it] + nextState.guessingScores[it] }
                                .toKoneUIntArray(),
                        )
                    }
                    is GameStateMachine.State.RoundEditing<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, WordsProviderDescription, *> -> {
                        val gameMachineSettings = nextState.settings
                        Outgoing.State.Round.Editing(
                            roomMetadata = metadata,
                            role = Outgoing.Role.Round.Editing(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                                roundRole = when (index) {
                                    nextState.speakerIndex -> Outgoing.Role.Round.Editing.RoundRole.Speaker(nextState.currentExplanationResults)
                                    nextState.listenerIndex -> Outgoing.Role.Round.Editing.RoundRole.Listener
                                    else -> Outgoing.Role.Round.Editing.RoundRole.Player
                                },
                                roundsBeforeSpeaking = nextState.playersRoundsBeforeSpeaking[index],
                                roundsBeforeListening = nextState.playersRoundsBeforeListening[index],
                            ),
                            playersList = playersList.mapIndexed { index, player ->
                                Outgoing.PlayerDescription.Round.Editing(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                    roundRole = when (index) {
                                        nextState.speakerIndex -> Outgoing.PlayerDescription.Round.RoundRole.Speaker
                                        nextState.listenerIndex -> Outgoing.PlayerDescription.Round.RoundRole.Listener
                                        else -> Outgoing.PlayerDescription.Round.RoundRole.Player
                                    },
                                    scoreExplained = nextState.explanationScores[index],
                                    scoreGuessed = nextState.guessingScores[index],
                                    scoreSum = nextState.explanationScores[index] + nextState.guessingScores[index],
                                )
                            },
                            settings = GameSettings(
                                preparationTimeSeconds = gameMachineSettings.preparationTimeSeconds,
                                explanationTimeSeconds = gameMachineSettings.explanationTimeSeconds,
                                finalGuessTimeSeconds = gameMachineSettings.finalGuessTimeSeconds,
                                strictMode = gameMachineSettings.strictMode,
                                gameEndCondition = gameMachineSettings.gameEndCondition,
                                wordsSource = when (val wordsSource = gameMachineSettings.wordsSource) {
                                    GameStateMachine.WordsSource.Players -> WordsSource.Players
                                    is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                                },
                            ),
                            initialWordsNumber = nextState.initialWordsNumber,
                            roundNumber =  nextState.roundNumber,
                            cycleNumber = nextState.cycleNumber,
                            speakerIndex = nextState.speakerIndex,
                            listenerIndex = nextState.listenerIndex,
                            nextSpeakerIndex = nextState.nextSpeakerIndex,
                            nextListenerIndex = nextState.nextListenerIndex,
                            restWordsNumber = nextState.restWords.size,
                            wordsInProgressNumber = nextState.currentExplanationResults.size,
                            wordsStatistic = nextState.wordsStatistic.toOutgoingAPI().filter { it.state != GameStateMachine.WordStatistic.State.InProgress },
                            leaderboardPermutation = playersList
                                .indices
                                .sortedByDescending { nextState.explanationScores[it] + nextState.guessingScores[it] }
                                .toKoneUIntArray(),
                        )
                    }
                    is GameStateMachine.State.GameResults<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, WordsProviderDescription, *> -> {
                        val gameMachineSettings = nextState.settings
                        Outgoing.State.GameResults(
                            roomMetadata = metadata,
                            role = Outgoing.Role.GameResults(
                                metadata = player.metadata,
                                userIndex = index,
                                isHost = index == hostIndex,
                            ),
                            playersList = playersList.mapIndexed { index, player ->
                                Outgoing.PlayerDescription.GameResults(
                                    metadata = player.metadata,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                    scoreExplained = nextState.results[index].scoreExplained,
                                    scoreGuessed = nextState.results[index].scoreGuessed,
                                    scoreSum = nextState.results[index].scoreSum,
                                )
                            },
                            settings = GameSettings(
                                preparationTimeSeconds = gameMachineSettings.preparationTimeSeconds,
                                explanationTimeSeconds = gameMachineSettings.explanationTimeSeconds,
                                finalGuessTimeSeconds = gameMachineSettings.finalGuessTimeSeconds,
                                strictMode = gameMachineSettings.strictMode,
                                gameEndCondition = gameMachineSettings.gameEndCondition,
                                wordsSource = when (val wordsSource = gameMachineSettings.wordsSource) {
                                    GameStateMachine.WordsSource.Players -> WordsSource.Players
                                    is GameStateMachine.WordsSource.Custom<WordsProviderDescription> -> WordsSource.Custom(wordsSource.providerId)
                                },
                            ),
                            leaderboardPermutation = playersList
                                .indices
                                .sortedByDescending { nextState.results[it].scoreSum }
                                .toKoneUIntArray(),
                            wordsStatistic = nextState.wordsStatistic.toOutgoingAPI(),
                        )
                    }
                }
                player.connectionsRegistry.forEach { it.sendNewState(gameStateToSend) }
            }
        }
    
    public val description: Description<RoomMetadata, PlayerMetadata>
        get() {
            val state = gameStateMachine.state
            val hostIndex = state.playersList.firstIndexThat { _, player -> player.isOnline }
            val playersList = state.playersList.mapIndexed { index, player ->
                Player.Description(metadata = player.metadata, isOnline = player.isOnline, isHost = index == hostIndex)
            }
            return Description(
                metadata = metadata,
                playersList = playersList,
                stateType = when (state) {
                    is GameStateMachine.State.GameInitialisation -> StateType.GameInitialisation
                    is GameStateMachine.State.PlayersWordsCollection -> StateType.PlayersWordsCollection
                    is GameStateMachine.State.RoundWaiting -> StateType.RoundWaiting
                    is GameStateMachine.State.RoundPreparation -> StateType.RoundPreparation
                    is GameStateMachine.State.RoundExplanation -> StateType.RoundExplanation
                    is GameStateMachine.State.RoundLastGuess -> StateType.RoundLastGuess
                    is GameStateMachine.State.RoundEditing -> StateType.RoundEditing
                    is GameStateMachine.State.GameResults -> StateType.GameResults
                }
            )
        }
    
    private val GameStateMachine.State<
        Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>,
        WordsProviderDescription,
        GameStateMachineMetadata<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>
    >.host: Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>?
        get() = playersList.let { if (it.isEmpty()) null else it.first() }
    
    public suspend fun attachConnectionToPlayer(connection: ConnectionType, playerID: PlayerID): Player.Attachment<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>? {
        val result = gameStateMachine.moveMaybeAndCompute { previousState ->
            when (previousState) {
                is GameStateMachine.State.GameInitialisation<Player<RoomMetadata, PlayerID, PlayerMetadata, WordsProviderID, WordsProviderDescription, NoWordsProviderReason, ConnectionType>, WordsProviderDescription, *> -> {
                    val player = previousState.metadata.allPlayersList.firstThatOrNull { it.metadata.id == playerID }
                    
                    if (player == null) {
                        val metadata = initialMetadataFactory(playerID)
                        if (checkConnectionAttachment(metadata, false, connection)) {
                            val newPlayer = Player(this, metadata)
                            val newAllPlayersList = KoneList.build {
                                addAllFrom(previousState.metadata.allPlayersList)
                                +newPlayer
                            }
                            val node = newPlayer.connectionsRegistry.addNode(connection)
                            val attachment = Player.Attachment(newPlayer, node)
                            
                            TransitionOrReasonAndComputation.Success(
                                GameStateMachine.Transition.UpdateGameSettings(
                                    playersList = newAllPlayersList.filter { it.isOnline },
                                    settingsBuilder = previousState.settingsBuilder,
                                    metadataTransition = GameStateMachineMetadataTransition.UpdateAllPlayersList(newAllPlayersList)
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
                                GameStateMachine.Transition.UpdateGameSettings(
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
                is GameStateMachine.State.PlayersWordsCollection<*, *, *>,
                is GameStateMachine.State.RoundWaiting<*, *, *>,
                is GameStateMachine.State.RoundPreparation<*, *, *>,
                is GameStateMachine.State.RoundExplanation<*, *, *>,
                is GameStateMachine.State.RoundLastGuess<*, *, *>,
                is GameStateMachine.State.RoundEditing<*, *, *>,
                is GameStateMachine.State.GameResults<*, *, *>,
                    -> {
                    val player = previousState.playersList.firstThatOrNull { it.metadata.id == playerID }
                    if (player == null || !checkConnectionAttachment(player.metadata, player.isOnline, connection)) {
                        connection.sendError(Outgoing.Error.AttachmentIsDenied)
                        
                        TransitionOrReasonAndComputation.Failure(null, null)
                    } else {
                        val node = player.connectionsRegistry.addNode(connection)
                        val attachment = Player.Attachment(player, node)
                        
                        TransitionOrReasonAndComputation.Success(
                            GameStateMachine.Transition.NoOperation(),
                            attachment,
                        )
                    }
                }
            }
        }
        when (result) {
            is MovementMaybeAndComputationResult.NoTransition<*, *, *> -> {}
            is MovementMaybeAndComputationResult.NoNextState<*, *, *, *> -> {} // TODO: Log transition denial
            is MovementMaybeAndComputationResult.Success -> {}
        }
        return result.computation
    }
}