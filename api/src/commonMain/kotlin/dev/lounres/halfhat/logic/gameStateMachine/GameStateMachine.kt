package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.collections.array.KoneUIntArray
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.set.KoneSet
import kotlinx.serialization.Serializable
import kotlin.time.Duration
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
        // TODO: Maybe not exactly this `number` of words but no more than this number.
        public fun randomWords(number: UInt): KoneSet<String>
        public fun allWords(): KoneSet<String>
    }
    
    public interface WordsProviderRegistry<in WPD, out Reason> {
        public suspend fun getWordsProvider(providerId: WPD): WordsProviderOrReason<Reason>
        
        public sealed interface WordsProviderOrReason<out Reason> {
            public data class Success(val result: WordsProvider) : WordsProviderOrReason<Nothing>
            public data class Failure<Reason>(val reason: Reason) : WordsProviderOrReason<Reason>
        }
    }
    
    @Serializable
    public sealed interface WordsSource<out WPD> {
        @Serializable
        public data object Players : WordsSource<Nothing>
        @Serializable
        public data class Custom<out WPD>(val providerId: WPD) : WordsSource<WPD>
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

    @Serializable
    public data class WordStatistic(
        val spentTime: Duration,
        val state: State,
    ) {
        @Serializable
        public enum class State {
            Explained, Mistake, InProgress;
        }

        @Serializable
        public data class AndWord(
            val word: String,
            val spentTime: Duration,
            val state: State,
        )
    }

    public data class GameSettings<out WPD>(
        val preparationTimeSeconds: UInt,
        val explanationTimeSeconds: UInt,
        val finalGuessTimeSeconds: UInt,
        val strictMode: Boolean,
        val gameEndCondition: GameEndCondition,
        val wordsSource: WordsSource<WPD>,
    ) {
        @Serializable
        public data class Builder<out WPD>(
            val preparationTimeSeconds: UInt,
            val explanationTimeSeconds: UInt,
            val finalGuessTimeSeconds: UInt,
            val strictMode: Boolean,
            val cachedEndConditionWordsNumber: UInt,
            val cachedEndConditionCyclesNumber: UInt,
            val gameEndConditionType: GameEndCondition.Type,
            val wordsSource: WordsSource<WPD>,
        ) {
            public fun build(): GameSettings<WPD> =
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
        val scoreSum: UInt,
    )

    public data class PersonalResult<P>(
        val player: P,
        val scoreExplained: UInt,
        val scoreGuessed: UInt,
        val scoreSum: UInt,
    )

    public sealed interface State<out P, out WPD> {
        public val playersList: KoneList<P>

        public data class GameInitialisation<out P, out WPD>(
            override val playersList: KoneList<P>,
            val settingsBuilder: GameSettings.Builder<WPD>,
        ) : State<P, WPD>

        public sealed interface GameInitialised<out P, out WPD> : State<P, WPD> {
            override val playersList: KoneList<P>
            public val settings: GameSettings<WPD>

            public data class PlayersWordsCollection<out P, out WPD>(
                override val playersList: KoneList<P>,
                override val settings: GameSettings<WPD>,
                val playersWords: KoneList<KoneSet<String>?>,
            ) : GameInitialised<P, WPD>

            public sealed interface Round<out P, out WPD> : GameInitialised<P, WPD> {
                override val playersList: KoneList<P>
                override val settings: GameSettings<WPD>
                public val initialWordsNumber: UInt
                public val roundNumber: UInt
                public val cycleNumber: UInt
                public val speakerIndex: UInt
                public val listenerIndex: UInt
                public val nextSpeakerIndex: UInt
                public val nextListenerIndex: UInt
                public val playersRoundsBeforeSpeaking: KoneUIntArray
                public val playersRoundsBeforeListening: KoneUIntArray
                public val restWords: KoneSet<String>
                public val explanationScores: KoneList<UInt>
                public val guessingScores: KoneList<UInt>
                public val wordsStatistic: KoneMap<String, WordStatistic>

                public data class RoundWaiting<out P, out WPD>(
                    override val playersList: KoneList<P>,
                    override val settings: GameSettings<WPD>,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val playersRoundsBeforeSpeaking: KoneUIntArray,
                    override val playersRoundsBeforeListening: KoneUIntArray,
                    override val restWords: KoneSet<String>,
                    override val explanationScores: KoneList<UInt>,
                    override val guessingScores: KoneList<UInt>,
                    override val wordsStatistic: KoneMap<String, WordStatistic>,
                    val speakerReady: Boolean,
                    val listenerReady: Boolean,
                ) : Round<P, WPD>

                public data class RoundPreparation<out P, out WPD>(
                    override val playersList: KoneList<P>,
                    override val settings: GameSettings<WPD>,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val playersRoundsBeforeSpeaking: KoneUIntArray,
                    override val playersRoundsBeforeListening: KoneUIntArray,
                    val startInstant: Instant,
                    val millisecondsLeft: UInt,
                    override val restWords: KoneSet<String>,
                    override val explanationScores: KoneList<UInt>,
                    override val guessingScores: KoneList<UInt>,
                    override val wordsStatistic: KoneMap<String, WordStatistic>,
                    val currentExplanationResults: KoneList<WordExplanation>,
                ) : Round<P, WPD>

                public data class RoundExplanation<out P, out WPD>(
                    override val playersList: KoneList<P>,
                    override val settings: GameSettings<WPD>,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val playersRoundsBeforeSpeaking: KoneUIntArray,
                    override val playersRoundsBeforeListening: KoneUIntArray,
                    val startInstant: Instant,
                    val millisecondsLeft: UInt,
                    override val restWords: KoneSet<String>,
                    val currentWord: String,
                    override val explanationScores: KoneList<UInt>,
                    override val guessingScores: KoneList<UInt>,
                    override val wordsStatistic: KoneMap<String, WordStatistic>,
                    val wordExplanationStart: Instant,
                    val currentExplanationResults: KoneList<WordExplanation>,
                ) : Round<P, WPD>

                public data class RoundLastGuess<out P, out WPD>(
                    override val playersList: KoneList<P>,
                    override val settings: GameSettings<WPD>,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val playersRoundsBeforeSpeaking: KoneUIntArray,
                    override val playersRoundsBeforeListening: KoneUIntArray,
                    val startInstant: Instant,
                    val millisecondsLeft: UInt,
                    override val restWords: KoneSet<String>,
                    val currentWord: String,
                    override val explanationScores: KoneList<UInt>,
                    override val guessingScores: KoneList<UInt>,
                    override val wordsStatistic: KoneMap<String, WordStatistic>,
                    val wordExplanationStart: Instant,
                    val currentExplanationResults: KoneList<WordExplanation>,
                ) : Round<P, WPD>

                public data class RoundEditing<out P, out WPD>(
                    override val playersList: KoneList<P>,
                    override val settings: GameSettings<WPD>,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val playersRoundsBeforeSpeaking: KoneUIntArray,
                    override val playersRoundsBeforeListening: KoneUIntArray,
                    override val restWords: KoneSet<String>,
                    override val explanationScores: KoneList<UInt>,
                    override val guessingScores: KoneList<UInt>,
                    override val wordsStatistic: KoneMap<String, WordStatistic>,
                    val currentExplanationResults: KoneList<WordExplanation>,
                ) : Round<P, WPD>
            }

            public data class GameResults<out P, out WPD>(
                override val playersList: KoneList<P>,
                override val settings: GameSettings<WPD>,
                val explanationScores: KoneList<UInt>,
                val guessingScores: KoneList<UInt>,
                val wordsStatistic: KoneMap<String, WordStatistic>,
            ) : GameInitialised<P, WPD>
        }
    }

    public sealed interface Transition<out P, out WPD, out NoWordsProviderReason> {
        public data class UpdateGameSettings<out P, out WPD>(
            public val playersList: KoneList<P>,
            public val settingsBuilder: GameSettings.Builder<WPD>,
        ) : Transition<P, WPD, Nothing>
        public data class InitialiseGame<WPD, out NoWordsProviderReason>(
            val wordsProviderRegistry: WordsProviderRegistry<WPD, NoWordsProviderReason>,
        ) : Transition<Nothing, WPD, NoWordsProviderReason>
        public data class SubmitPlayerWords(
            public val playerIndex: UInt,
            public val playerWords: KoneSet<String>,
        ) : Transition<Nothing, Nothing, Nothing>
        public data object SpeakerReady : Transition<Nothing, Nothing, Nothing>
        public data object ListenerReady : Transition<Nothing, Nothing, Nothing>
        public data object SpeakerAndListenerReady : Transition<Nothing, Nothing, Nothing>
        public data class UpdateRoundInfo(
            public val stopTimer: () -> Unit,
            public val roundNumber: UInt,
        ) : Transition<Nothing, Nothing, Nothing>
        public data class WordExplanationState(
            public val wordState: WordExplanation.State,
        ) : Transition<Nothing, Nothing, Nothing>
        public data class UpdateWordsExplanationResults(
            public val newExplanationResults: KoneList<WordExplanation>,
        ) : Transition<Nothing, Nothing, Nothing>
        public data object ConfirmWordsExplanationResults: Transition<Nothing, Nothing, Nothing>
        public data object FinishGame: Transition<Nothing, Nothing, Nothing>
    }
    
    public sealed interface NoNextStateReason<out NoWordsProviderReason> {
        public data class NoWordsProvider<out NoWordsProviderReason>(public val reason: NoWordsProviderReason) : NoNextStateReason<NoWordsProviderReason>
        public data object CannotUpdateGameSettingsAfterInitialization : NoNextStateReason<Nothing>
        public data object NotEnoughPlayersForInitialization : NoNextStateReason<Nothing>
        public data object CannotInitializeGameAfterInitialization : NoNextStateReason<Nothing>
        public data object PlayerAlreadySubmittedWords : NoNextStateReason<Nothing>
        public data object CannotSubmitPlayerWordsNotDuringPlayersWordsCollection : NoNextStateReason<Nothing>
        public data object CannotSetSpeakerReadinessNotDuringRoundWaiting : NoNextStateReason<Nothing>
        public data object CannotSetListenerReadinessNotDuringRoundWaiting : NoNextStateReason<Nothing>
        public data object CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting : NoNextStateReason<Nothing>
        public data object CannotUpdateRoundInfoNotDuringTheRound : NoNextStateReason<Nothing>
        public data object CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess : NoNextStateReason<Nothing>
        public data object CannotUpdateWordExplanationResultsNotDuringRoundEditing : NoNextStateReason<Nothing>
        public data object CannotUpdateWordExplanationResultsWithOtherWordsSet : NoNextStateReason<Nothing>
        public data object CannotConfirmWordExplanationResultsNotDuringRoundEditing : NoNextStateReason<Nothing>
        public data object CannotFinishGameNotDuringRoundWaiting : NoNextStateReason<Nothing>
    }
}