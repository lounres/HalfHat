package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.set.KoneSet
import kotlinx.coroutines.Job
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


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
        // TODO: Revise this part of API
        public val size: UInt
        public fun randomWords(number: UInt): KoneSet<String>
        public fun allWords(): KoneSet<String>
    }
    
    public sealed interface WordsSource<out WP: WordsProvider> {
        public data object Players : WordsSource<Nothing>
        public data class Custom<out WP: WordsProvider>(val provider: WP) : WordsSource<WP>
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
    
    public data class GameSettings<out WP: WordsProvider>(
        val preparationTimeSeconds: UInt,
        val explanationTimeSeconds: UInt,
        val finalGuessTimeSeconds: UInt,
        val strictMode: Boolean,
        val gameEndCondition: GameEndCondition,
        val wordsSource: WordsSource<WP>,
    ) {
        public data class Builder<out WP: WordsProvider>(
            val preparationTimeSeconds: UInt,
            val explanationTimeSeconds: UInt,
            val finalGuessTimeSeconds: UInt,
            val strictMode: Boolean,
            val cachedEndConditionWordsNumber: UInt,
            val cachedEndConditionCyclesNumber: UInt,
            val gameEndConditionType: GameEndCondition.Type,
            val wordsSource: WordsSource<WP>,
        ) {
            public fun build(): GameSettings<WP> =
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
    
    public sealed interface State<out P, out WP: WordsProvider, out Metadata> {
        public val metadata: Metadata
        public val playersList: KoneList<P>
        
        public data class GameInitialisation<out P, out WP: WordsProvider, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settingsBuilder: GameSettings.Builder<WP>,
        ) : State<P, WP, Metadata>
        
        public data class RoundWaiting<out P, out WP: WordsProvider, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WP>,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val restWords: KoneSet<String>,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val speakerReady: Boolean,
            val listenerReady: Boolean,
        ) : State<P, WP, Metadata>
        
        public data class RoundPreparation<out P, out WP: WordsProvider, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WP>,
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
            val timer: Job,
        ) : State<P, WP, Metadata>
        
        public data class RoundExplanation<out P, out WP: WordsProvider, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WP>,
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
            val timer: Job,
        ) : State<P, WP, Metadata>
        
        public data class RoundLastGuess<out P, out WP: WordsProvider, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WP>,
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
            val timer: Job,
        ) : State<P, WP, Metadata>
        
        public data class RoundEditing<out P, out WP: WordsProvider, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WP>,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val restWords: KoneSet<String>,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val currentExplanationResults: KoneList<WordExplanation>,
        ) : State<P, WP, Metadata>
        
        public data class GameResults<out P, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val results: KoneList<GameResult>,
        ) : State<P, Nothing, Metadata>
    }
    
    public sealed interface Transition<out P, out WP: WordsProvider, out MetadataTransition> {
        public data class UpdateMetadata<out MetadataTransition>(
            public val metadataTransition: MetadataTransition,
        ) : Transition<Nothing, Nothing, MetadataTransition>
        public sealed interface UpdateGame<out P, out WP: WordsProvider> : Transition<P, WP, Nothing> {
            public data class UpdateGameSettings<out P, out WP : WordsProvider>(
                public val playersList: KoneList<P>,
                public val settingsBuilder: GameSettings.Builder<WP>,
            ) : UpdateGame<P, WP>
            public data object InitialiseGame : UpdateGame<Nothing, Nothing>
            public data object SpeakerReady : UpdateGame<Nothing, Nothing>
            public data object ListenerReady : UpdateGame<Nothing, Nothing>
            public data object SpeakerAndListenerReady : UpdateGame<Nothing, Nothing>
            public data class UpdateRoundInfo(
                public val roundNumber: UInt,
            ) : UpdateGame<Nothing, Nothing>
            public data class WordExplanationState(
                public val wordState: WordExplanation.State,
            ) : UpdateGame<Nothing, Nothing>
            public data class UpdateWordsExplanationResults(
                public val newExplanationResults: KoneList<WordExplanation>,
            ) : UpdateGame<Nothing, Nothing>
            public data object ConfirmWordsExplanationResults : UpdateGame<Nothing, Nothing>
            public data object FinishGame : UpdateGame<Nothing, Nothing>
        }
    }
    
    public sealed interface NoNextStateReason<out NoMetadataTransitionReason> {
        public data class NoMetadataUpdate<out NoMetadataTransitionReason>(public val reason: NoMetadataTransitionReason) : NoNextStateReason<NoMetadataTransitionReason>
        public data object CannotUpdateGameSettingsAfterInitialization : NoNextStateReason<Nothing>
        public data object NotEnoughPlayersForInitialization : NoNextStateReason<Nothing>
        public data object CannotInitializationGameSettingsAfterInitialization : NoNextStateReason<Nothing>
        public data object CannotSetSpeakerReadinessNotDuringRoundWaiting : NoNextStateReason<Nothing>
        public data object CannotSetListenerReadinessNotDuringRoundWaiting : NoNextStateReason<Nothing>
        public data object CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting : NoNextStateReason<Nothing>
        public data object CannotUpdateRoundInfoNotDuringTheRound : NoNextStateReason<Nothing>
        public data object CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess : NoNextStateReason<Nothing>
        public data object CannotUpdateWordExplanationResultsNotDuringRoundEditing : NoNextStateReason<Nothing>
        public data object CannotConfirmWordExplanationResultsNotDuringRoundEditing : NoNextStateReason<Nothing>
        public data object CannotFinishGameNotDuringRoundWaiting : NoNextStateReason<Nothing>
    }
}