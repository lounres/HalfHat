package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.set.KoneSet
import kotlinx.coroutines.Job
import kotlinx.serialization.Serializable
import kotlin.time.Instant


public object GameStateMachine {
    @Serializable
    public sealed interface GameEndCondition {
        @Serializable
        public data class Words(val number: UInt) : GameEndCondition
        @Serializable
        public data class Cycles(val number: UInt) : GameEndCondition
        
        @Serializable
        public enum class Type {
            Words, Cycles,
        }
    }
    
    public interface WordsProvider {
        public val size: UInt
        public fun randomWords(number: UInt): KoneSet<String>
        public fun allWords(): KoneSet<String>
    }
    
    public interface WordsProviderRegistry<in WPID, out Reason> {
        public suspend operator fun get(providerId: WPID): ResultOrReason<Reason>
        
        public sealed interface ResultOrReason<out Reason> {
            public data class Success(val result: WordsProvider) : ResultOrReason<Nothing>
            public data class Failure<Reason>(val reason: Reason) : ResultOrReason<Reason>
        }
    }
    
    public sealed interface WordsSource<out WPID> {
        public data object Players : WordsSource<Nothing>
        public data class Custom<out WPID>(val providerId: WPID) : WordsSource<WPID>
    }
    
    @Serializable
    public data class WordExplanation(
        val word: String,
        val state: State
    ) {
        @Serializable
        public enum class State {
            Explained, Mistake, NotExplained;
        }
    }
    
    public data class GameSettings<out WPID>(
        val preparationTimeSeconds: UInt,
        val explanationTimeSeconds: UInt,
        val finalGuessTimeSeconds: UInt,
        val strictMode: Boolean,
        val gameEndCondition: GameEndCondition,
        val wordsSource: WordsSource<WPID>,
    ) {
        public data class Builder<out WPID>(
            val preparationTimeSeconds: UInt,
            val explanationTimeSeconds: UInt,
            val finalGuessTimeSeconds: UInt,
            val strictMode: Boolean,
            val cachedEndConditionWordsNumber: UInt,
            val cachedEndConditionCyclesNumber: UInt,
            val gameEndConditionType: GameEndCondition.Type,
            val wordsSource: WordsSource<WPID>,
        ) {
            public fun build(): GameSettings<WPID> =
                GameSettings(
                    preparationTimeSeconds = preparationTimeSeconds,
                    explanationTimeSeconds = explanationTimeSeconds,
                    finalGuessTimeSeconds = finalGuessTimeSeconds,
                    strictMode = strictMode,
                    gameEndCondition = when (gameEndConditionType) {
                        GameEndCondition.Type.Words -> GameEndCondition.Words(cachedEndConditionWordsNumber)
                        GameEndCondition.Type.Cycles -> GameEndCondition.Cycles(cachedEndConditionCyclesNumber)
                    },
                    wordsSource = wordsSource,
                )
        }
    }
    
    @Serializable
    public data class GameResult(
        val player: UInt,
        val scoreExplained: UInt,
        val scoreGuessed: UInt,
        val sum: UInt,
    )
    
    public data class PersonalResult<P>(
        val player: P,
        val scoreExplained: UInt,
        val scoreGuessed: UInt,
        val sum: UInt,
    )
    
    public sealed interface State<out P, out WPID, out Metadata> {
        public val metadata: Metadata
        public val playersList: KoneList<P>
        
        public data class GameInitialisation<out P, out WPID, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settingsBuilder: GameSettings.Builder<WPID>,
        ) : State<P, WPID, Metadata>
        
        public data class RoundWaiting<out P, out WPID, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WPID>,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val restWords: KoneSet<String>,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val speakerReady: Boolean,
            val listenerReady: Boolean,
        ) : State<P, WPID, Metadata>
        
        public data class RoundPreparation<out P, out WPID, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WPID>,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val startInstant: Instant,
            val millisecondsLeft: UInt,
            val restWords: KoneSet<String>,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val currentExplanationResults: KoneList<WordExplanation>,
        ) : State<P, WPID, Metadata>
        
        public data class RoundExplanation<out P, out WPID, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WPID>,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val startInstant: Instant,
            val millisecondsLeft: UInt,
            val restWords: KoneSet<String>,
            val currentWord: String,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val currentExplanationResults: KoneList<WordExplanation>,
        ) : State<P, WPID, Metadata>
        
        public data class RoundLastGuess<out P, out WPID, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WPID>,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val startInstant: Instant,
            val millisecondsLeft: UInt,
            val restWords: KoneSet<String>,
            val currentWord: String,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val currentExplanationResults: KoneList<WordExplanation>,
        ) : State<P, WPID, Metadata>
        
        public data class RoundEditing<out P, out WPID, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WPID>,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val restWords: KoneSet<String>,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val currentExplanationResults: KoneList<WordExplanation>,
        ) : State<P, WPID, Metadata>
        
        public data class GameResults<out P, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val results: KoneList<GameResult>,
        ) : State<P, Nothing, Metadata>
    }
    
    public sealed interface Transition<out P, out WPID, out NoWordsProviderReason, out MetadataTransition> {
        public data class UpdateMetadata<out MetadataTransition>(
            public val metadataTransition: MetadataTransition,
        ) : Transition<Nothing, Nothing, Nothing, MetadataTransition>
        public sealed interface UpdateGame<out P, out WPID, out NoWordsProviderReason> : Transition<P, WPID, NoWordsProviderReason, Nothing> {
            public data class UpdateGameSettings<out P, out WPID>(
                public val playersList: KoneList<P>,
                public val settingsBuilder: GameSettings.Builder<WPID>,
            ) : UpdateGame<P, WPID, Nothing>
            public data class InitialiseGame<WPID, out NoWordsProviderReason>(
                val wordsProviderRegistry: WordsProviderRegistry<WPID, NoWordsProviderReason>
            ) : UpdateGame<Nothing, WPID, NoWordsProviderReason>
            public data object SpeakerReady : UpdateGame<Nothing, Nothing, Nothing>
            public data object ListenerReady : UpdateGame<Nothing, Nothing, Nothing>
            public data object SpeakerAndListenerReady : UpdateGame<Nothing, Nothing, Nothing>
            public data class UpdateRoundInfo(
                public val timer: Job,
                public val roundNumber: UInt,
            ) : UpdateGame<Nothing, Nothing, Nothing>
            public data class WordExplanationState(
                public val wordState: WordExplanation.State,
            ) : UpdateGame<Nothing, Nothing, Nothing>
            public data class UpdateWordsExplanationResults(
                public val newExplanationResults: KoneList<WordExplanation>,
            ) : UpdateGame<Nothing, Nothing, Nothing>
            public data object ConfirmWordsExplanationResults : UpdateGame<Nothing, Nothing, Nothing>
            public data object FinishGame : UpdateGame<Nothing, Nothing, Nothing>
        }
    }
    
    public sealed interface NoNextStateReason<out NoMetadataTransitionReason, out NoWordsProviderReason> {
        public data class NoMetadataUpdate<out NoMetadataTransitionReason>(public val reason: NoMetadataTransitionReason) : NoNextStateReason<NoMetadataTransitionReason, Nothing>
        public data class NoWordsProvider<out NoWordsProviderReason>(public val reason: NoWordsProviderReason) : NoNextStateReason<Nothing, NoWordsProviderReason>
        public data object CannotUpdateGameSettingsAfterInitialization : NoNextStateReason<Nothing, Nothing>
        public data object NotEnoughPlayersForInitialization : NoNextStateReason<Nothing, Nothing>
        public data object CannotInitializationGameSettingsAfterInitialization : NoNextStateReason<Nothing, Nothing>
        public data object CannotSetSpeakerReadinessNotDuringRoundWaiting : NoNextStateReason<Nothing, Nothing>
        public data object CannotSetListenerReadinessNotDuringRoundWaiting : NoNextStateReason<Nothing, Nothing>
        public data object CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting : NoNextStateReason<Nothing, Nothing>
        public data object CannotUpdateRoundInfoNotDuringTheRound : NoNextStateReason<Nothing, Nothing>
        public data object CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess : NoNextStateReason<Nothing, Nothing>
        public data object CannotUpdateWordExplanationResultsNotDuringRoundEditing : NoNextStateReason<Nothing, Nothing>
        public data object CannotConfirmWordExplanationResultsNotDuringRoundEditing : NoNextStateReason<Nothing, Nothing>
        public data object CannotFinishGameNotDuringRoundWaiting : NoNextStateReason<Nothing, Nothing>
    }
}