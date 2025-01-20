package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.halfhat.utils.scope
import dev.lounres.kone.collections.iterables.isEmpty
import dev.lounres.kone.collections.iterables.isNotEmpty
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.KoneSettableList
import dev.lounres.kone.collections.list.emptyKoneList
import dev.lounres.kone.collections.list.indices
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.buildKoneSet
import dev.lounres.kone.collections.set.koneMutableSetOf
import dev.lounres.kone.collections.utils.count
import dev.lounres.kone.collections.utils.filter
import dev.lounres.kone.collections.utils.filterTo
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.collections.utils.plusAssign
import dev.lounres.kone.collections.utils.random
import dev.lounres.kone.collections.utils.sortByDescending
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.random.Random


internal val DefaultRandom: Random = Random

public class GameStateMachine<P, WP: GameStateMachine.WordsProvider> internal constructor(
    initialState: State<P, WP>,
    private val coroutineScope: CoroutineScope,
    private val structuralMutex: Mutex,
    private val random: Random = DefaultRandom,
) {
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
    
    public data class GameSettings(
        val preparationTimeSeconds: UInt,
        val explanationTimeSeconds: UInt,
        val finalGuessTimeSeconds: UInt,
        val strictMode: Boolean,
        val gameEndCondition: GameEndCondition,
    )
    
    public data class GameSettingsBuilder<out WP: WordsProvider>(
        val preparationTimeSeconds: UInt,
        val explanationTimeSeconds: UInt,
        val finalGuessTimeSeconds: UInt,
        val strictMode: Boolean,
        val cachedEndConditionWordsNumber: UInt,
        val cachedEndConditionCyclesNumber: UInt,
        val gameEndConditionType: GameEndCondition.Type,
        val wordsSource: WordsSource<WP>,
    )
    
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
    
    public sealed interface State<out P, out WP: WordsProvider> {
        public data class GameInitialisation<out P, out WP: WordsProvider>(
            val playersList: KoneList<P>,
            val settingsBuilder: GameSettingsBuilder<WP>,
        ) : State<P, WP>
        
        public data class RoundWaiting<out P>(
            val playersList: KoneList<P>,
            val settings: GameSettings,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val restWords: KoneSet<String>,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val speakerReady: Boolean,
            val listenerReady: Boolean,
        ) : State<P, Nothing>
        
        public data class RoundPreparation<out P>(
            val playersList: KoneList<P>,
            val settings: GameSettings,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val millisecondsLeft: UInt,
            val restWords: KoneSet<String>,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val currentExplanationResults: KoneList<WordExplanation>,
            val timer: Job,
        ) : State<P, Nothing>
        
        public data class RoundExplanation<out P>(
            val playersList: KoneList<P>,
            val settings: GameSettings,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val millisecondsLeft: UInt,
            val restWords: KoneSet<String>,
            val currentWord: String,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val currentExplanationResults: KoneList<WordExplanation>,
            val timer: Job,
        ) : State<P, Nothing>
        
        public data class RoundLastGuess<out P>(
            val playersList: KoneList<P>,
            val settings: GameSettings,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val millisecondsLeft: UInt,
            val restWords: KoneSet<String>,
            val currentWord: String,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val currentExplanationResults: KoneList<WordExplanation>,
            val timer: Job,
        ) : State<P, Nothing>
        
        public data class RoundEditing<out P>(
            val playersList: KoneList<P>,
            val settings: GameSettings,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val restWords: KoneSet<String>,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val currentExplanationResults: KoneList<WordExplanation>,
        ) : State<P, Nothing>
        
        public data class GameResults<out P>(
            val playersList: KoneList<P>,
            val results: KoneList<GameResult>,
        ) : State<P, Nothing>
    }
    
    public object Result {
        public sealed interface GameSettingsUpdateResult {
            public data object Success : GameSettingsUpdateResult
            public data object InvalidState : GameSettingsUpdateResult
        }
        
        public sealed interface GameInitialisationResult {
            public data object Success : GameInitialisationResult
            public data object InvalidState : GameInitialisationResult
        }
        
        public sealed interface SpeakerReadinessResult {
            public data object Success : SpeakerReadinessResult
            public data object InvalidState : SpeakerReadinessResult
        }
        
        public sealed interface ListenerReadinessResult {
            public data object Success : ListenerReadinessResult
            public data object InvalidState : ListenerReadinessResult
        }
        
        public sealed interface WordExplanationStatementResult {
            public data object Success : WordExplanationStatementResult
            public data object InvalidState : WordExplanationStatementResult
        }
        
        public sealed interface WordsExplanationResultsUpdateResult {
            public data object Success : WordsExplanationResultsUpdateResult
            public data object InvalidState : WordsExplanationResultsUpdateResult
        }
        
        public sealed interface WordsExplanationResultsConfirmationResult {
            public data object Success : WordsExplanationResultsConfirmationResult
            public data object InvalidState : WordsExplanationResultsConfirmationResult
        }
        
        public sealed interface GameFinishingResult {
            public data object Success : GameFinishingResult
            public data object InvalidState : GameFinishingResult
        }
    }
    
    private val _state: MutableStateFlow<State<P, WP>> = MutableStateFlow(initialState)
    public val state: StateFlow<State<P, WP>> get() = _state
    
    // TODO: Log strange cases
    private fun createTimer(
        roundNumber: UInt,
        startInstant: Instant,
        preparationTimeSeconds: UInt,
        explanationTimeSeconds: UInt,
        finalGuessTimeSeconds: UInt,
    ): Job = coroutineScope.launch {
        var terminalStage = false
        while (!terminalStage) scope { // TODO: Remove when non-local `break` and `continue` will be implemented
            structuralMutex.withLock {
                val currentInstant = Clock.System.now()
                val spentTimeMilliseconds = (currentInstant - startInstant).inWholeMilliseconds.toUInt()
                val newState = when (val state = state.value) {
                    is State.GameInitialisation -> return@scope
                    is State.RoundWaiting -> if (state.roundNumber != roundNumber) return@launch else return@scope
                    is State.RoundPreparation ->
                        when {
                            state.roundNumber != roundNumber -> return@launch
                            spentTimeMilliseconds < preparationTimeSeconds * 1000u ->
                                State.RoundPreparation(
                                    playersList = state.playersList,
                                    settings = state.settings,
                                    roundNumber = state.roundNumber,
                                    cycleNumber = state.cycleNumber,
                                    speakerIndex = state.speakerIndex,
                                    listenerIndex = state.listenerIndex,
                                    millisecondsLeft = preparationTimeSeconds * 1000u - spentTimeMilliseconds,
                                    restWords = state.restWords,
                                    explanationScores = state.explanationScores,
                                    guessingScores = state.guessingScores,
                                    currentExplanationResults = state.currentExplanationResults,
                                    timer = state.timer,
                                )
                            spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds) * 1000u -> {
                                val currentWord = state.restWords.random(random)
                                val restWords = state.restWords.filterTo(koneMutableSetOf()) { it != currentWord }
                                State.RoundExplanation(
                                    playersList = state.playersList,
                                    settings = state.settings,
                                    roundNumber = state.roundNumber,
                                    cycleNumber = state.cycleNumber,
                                    speakerIndex = state.speakerIndex,
                                    listenerIndex = state.listenerIndex,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = restWords,
                                    currentWord = currentWord,
                                    explanationScores = state.explanationScores,
                                    guessingScores = state.guessingScores,
                                    currentExplanationResults = state.currentExplanationResults,
                                    timer = state.timer,
                                )
                            }
                            spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u -> {
                                val currentWord = state.restWords.random(random)
                                val restWords = state.restWords.filterTo(koneMutableSetOf()) { it != currentWord }
                                State.RoundLastGuess(
                                    playersList = state.playersList,
                                    settings = state.settings,
                                    roundNumber = state.roundNumber,
                                    cycleNumber = state.cycleNumber,
                                    speakerIndex = state.speakerIndex,
                                    listenerIndex = state.listenerIndex,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = restWords,
                                    currentWord = currentWord,
                                    explanationScores = state.explanationScores,
                                    guessingScores = state.guessingScores,
                                    currentExplanationResults = state.currentExplanationResults,
                                    timer = state.timer,
                                )
                            }
                            else -> {
                                if (state.settings.strictMode)
                                    State.RoundEditing(
                                        playersList = state.playersList,
                                        settings = state.settings,
                                        roundNumber = state.roundNumber,
                                        cycleNumber = state.cycleNumber,
                                        speakerIndex = state.speakerIndex,
                                        listenerIndex = state.listenerIndex,
                                        restWords = state.restWords,
                                        explanationScores = state.explanationScores,
                                        guessingScores = state.guessingScores,
                                        currentExplanationResults = state.currentExplanationResults,
                                    ).also { terminalStage = true }
                                else {
                                    val currentWord = state.restWords.random(random)
                                    val restWords = state.restWords.filterTo(koneMutableSetOf()) { it != currentWord }
                                    State.RoundLastGuess(
                                        playersList = state.playersList,
                                        settings = state.settings,
                                        roundNumber = state.roundNumber,
                                        cycleNumber = state.cycleNumber,
                                        speakerIndex = state.speakerIndex,
                                        listenerIndex = state.listenerIndex,
                                        millisecondsLeft = 0u,
                                        restWords = restWords,
                                        currentWord = currentWord,
                                        explanationScores = state.explanationScores,
                                        guessingScores = state.guessingScores,
                                        currentExplanationResults = state.currentExplanationResults,
                                        timer = state.timer,
                                    )
                                }
                            }
                        }
                    is State.RoundExplanation ->
                        when {
                            state.roundNumber != roundNumber -> return@launch
                            spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds) * 1000u ->
                                State.RoundExplanation(
                                    playersList = state.playersList,
                                    settings = state.settings,
                                    roundNumber = state.roundNumber,
                                    cycleNumber = state.cycleNumber,
                                    speakerIndex = state.speakerIndex,
                                    listenerIndex = state.listenerIndex,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = state.restWords,
                                    currentWord = state.currentWord,
                                    explanationScores = state.explanationScores,
                                    guessingScores = state.guessingScores,
                                    currentExplanationResults = state.currentExplanationResults,
                                    timer = state.timer,
                                )
                            spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u ->
                                State.RoundLastGuess(
                                    playersList = state.playersList,
                                    settings = state.settings,
                                    roundNumber = state.roundNumber,
                                    cycleNumber = state.cycleNumber,
                                    speakerIndex = state.speakerIndex,
                                    listenerIndex = state.listenerIndex,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = state.restWords,
                                    currentWord = state.currentWord,
                                    explanationScores = state.explanationScores,
                                    guessingScores = state.guessingScores,
                                    currentExplanationResults = state.currentExplanationResults,
                                    timer = state.timer,
                                )
                            else -> {
                                if (state.settings.strictMode)
                                    State.RoundEditing(
                                        playersList = state.playersList,
                                        settings = state.settings,
                                        roundNumber = state.roundNumber,
                                        cycleNumber = state.cycleNumber,
                                        speakerIndex = state.speakerIndex,
                                        listenerIndex = state.listenerIndex,
                                        restWords = state.restWords,
                                        explanationScores = state.explanationScores,
                                        guessingScores = state.guessingScores,
                                        currentExplanationResults = KoneList(state.currentExplanationResults.size + 1u) {
                                            when (it) {
                                                in state.currentExplanationResults.indices -> state.currentExplanationResults[it]
                                                else -> WordExplanation(state.currentWord, WordExplanation.State.NotExplained)
                                            }
                                        },
                                    ).also { terminalStage = true }
                                else
                                    State.RoundLastGuess(
                                        playersList = state.playersList,
                                        settings = state.settings,
                                        roundNumber = state.roundNumber,
                                        cycleNumber = state.cycleNumber,
                                        speakerIndex = state.speakerIndex,
                                        listenerIndex = state.listenerIndex,
                                        millisecondsLeft = 0u,
                                        restWords = state.restWords,
                                        currentWord = state.currentWord,
                                        explanationScores = state.explanationScores,
                                        guessingScores = state.guessingScores,
                                        currentExplanationResults = state.currentExplanationResults,
                                        timer = state.timer,
                                    )
                            }
                        }
                    is State.RoundLastGuess ->
                        when {
                            state.roundNumber != roundNumber -> return@launch
                            spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u ->
                                State.RoundLastGuess(
                                    playersList = state.playersList,
                                    settings = state.settings,
                                    roundNumber = state.roundNumber,
                                    cycleNumber = state.cycleNumber,
                                    speakerIndex = state.speakerIndex,
                                    listenerIndex = state.listenerIndex,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = state.restWords,
                                    currentWord = state.currentWord,
                                    explanationScores = state.explanationScores,
                                    guessingScores = state.guessingScores,
                                    currentExplanationResults = state.currentExplanationResults,
                                    timer = state.timer,
                                )
                            else -> {
                                if (state.settings.strictMode)
                                    State.RoundEditing(
                                        playersList = state.playersList,
                                        settings = state.settings,
                                        roundNumber = state.roundNumber,
                                        cycleNumber = state.cycleNumber,
                                        speakerIndex = state.speakerIndex,
                                        listenerIndex = state.listenerIndex,
                                        restWords = state.restWords,
                                        explanationScores = state.explanationScores,
                                        guessingScores = state.guessingScores,
                                        currentExplanationResults = KoneList(state.currentExplanationResults.size + 1u) {
                                            when (it) {
                                                in state.currentExplanationResults.indices -> state.currentExplanationResults[it]
                                                else -> WordExplanation(state.currentWord, WordExplanation.State.NotExplained)
                                            }
                                        },
                                    ).also { terminalStage = true }
                                else
                                    State.RoundLastGuess(
                                        playersList = state.playersList,
                                        settings = state.settings,
                                        roundNumber = state.roundNumber,
                                        cycleNumber = state.cycleNumber,
                                        speakerIndex = state.speakerIndex,
                                        listenerIndex = state.listenerIndex,
                                        millisecondsLeft = 0u,
                                        restWords = state.restWords,
                                        currentWord = state.currentWord,
                                        explanationScores = state.explanationScores,
                                        guessingScores = state.guessingScores,
                                        currentExplanationResults = state.currentExplanationResults,
                                        timer = state.timer,
                                    )
                            }
                        }
                    is State.RoundEditing -> return@launch
                    is State.GameResults -> return@launch
                }
                _state.value = newState
            }
        }
    }
    
    public fun updateGameSettings(
        playersList: KoneList<P>,
        settingsBuilder: GameSettingsBuilder<WP>
    ): Result.GameSettingsUpdateResult {
        val newState = when (state.value) {
            is State.GameInitialisation ->
                State.GameInitialisation(
                    playersList = playersList,
                    settingsBuilder = settingsBuilder
                )
            
            is State.RoundWaiting,
            is State.RoundPreparation,
            is State.RoundExplanation,
            is State.RoundLastGuess,
            is State.RoundEditing,
            is State.GameResults,
                -> return Result.GameSettingsUpdateResult.InvalidState
        }
        _state.value = newState
        return Result.GameSettingsUpdateResult.Success
    }
    
    public fun initialiseGame(): Result.GameInitialisationResult {
        val newState = when (val currentState = state.value) {
            is State.GameInitialisation ->
                when (val wordsSource = currentState.settingsBuilder.wordsSource) {
                    WordsSource.Players -> TODO()
                    is WordsSource.Custom ->
                        State.RoundWaiting(
                            playersList = currentState.playersList,
                            settings = GameSettings(
                                preparationTimeSeconds = currentState.settingsBuilder.preparationTimeSeconds,
                                explanationTimeSeconds = currentState.settingsBuilder.explanationTimeSeconds,
                                finalGuessTimeSeconds = currentState.settingsBuilder.finalGuessTimeSeconds,
                                strictMode = currentState.settingsBuilder.strictMode,
                                gameEndCondition = when (currentState.settingsBuilder.gameEndConditionType) {
                                    GameEndCondition.Type.Words -> GameEndCondition.Words(currentState.settingsBuilder.cachedEndConditionWordsNumber)
                                    GameEndCondition.Type.Cycles -> GameEndCondition.Cycles(currentState.settingsBuilder.cachedEndConditionCyclesNumber)
                                },
                            ),
                            roundNumber = 0u,
                            cycleNumber = 0u,
                            speakerIndex = 0u,
                            listenerIndex = 1u,
                            restWords = when (currentState.settingsBuilder.gameEndConditionType) {
                                GameEndCondition.Type.Words -> wordsSource.provider.randomWords(currentState.settingsBuilder.cachedEndConditionWordsNumber)
                                GameEndCondition.Type.Cycles -> wordsSource.provider.allWords()
                            },
                            explanationScores = KoneList(currentState.playersList.size) { 0u },
                            guessingScores = KoneList(currentState.playersList.size) { 0u },
                            speakerReady = false,
                            listenerReady = false,
                        )
                }
            
            is State.RoundWaiting,
            is State.RoundPreparation,
            is State.RoundExplanation,
            is State.RoundLastGuess,
            is State.RoundEditing,
            is State.GameResults,
                -> return Result.GameInitialisationResult.InvalidState
        }
        _state.value = newState
        return Result.GameInitialisationResult.Success
    }
    
    public fun speakerReady(): Result.SpeakerReadinessResult {
        val newState = when (val currentState = state.value) {
            is State.RoundWaiting ->
                if (currentState.listenerReady) {
                    State.RoundPreparation(
                        playersList = currentState.playersList,
                        settings = currentState.settings,
                        roundNumber = currentState.roundNumber,
                        cycleNumber = currentState.cycleNumber,
                        speakerIndex = currentState.speakerIndex,
                        listenerIndex = currentState.listenerIndex,
                        millisecondsLeft = currentState.settings.preparationTimeSeconds * 1000u,
                        restWords = currentState.restWords,
                        explanationScores = currentState.explanationScores,
                        guessingScores = currentState.guessingScores,
                        currentExplanationResults = emptyKoneList(),
                        timer = createTimer(
                            roundNumber = currentState.roundNumber,
                            startInstant = Clock.System.now(),
                            preparationTimeSeconds = currentState.settings.preparationTimeSeconds,
                            explanationTimeSeconds = currentState.settings.explanationTimeSeconds,
                            finalGuessTimeSeconds = currentState.settings.finalGuessTimeSeconds,
                        )
                    )
                } else State.RoundWaiting(
                    playersList = currentState.playersList,
                    settings = currentState.settings,
                    roundNumber = currentState.roundNumber,
                    cycleNumber = currentState.cycleNumber,
                    speakerIndex = currentState.speakerIndex,
                    listenerIndex = currentState.listenerIndex,
                    restWords = currentState.restWords,
                    explanationScores = currentState.explanationScores,
                    guessingScores = currentState.guessingScores,
                    speakerReady = true,
                    listenerReady = currentState.listenerReady,
                )
            
            is State.GameInitialisation,
            is State.RoundPreparation,
            is State.RoundExplanation,
            is State.RoundLastGuess,
            is State.RoundEditing,
            is State.GameResults,
                -> return Result.SpeakerReadinessResult.InvalidState
        }
        _state.value = newState
        return Result.SpeakerReadinessResult.Success
    }
    
    public fun listenerReady(): Result.ListenerReadinessResult {
        val newState = when (val currentState = state.value) {
            is State.RoundWaiting ->
                if (currentState.speakerReady) {
                    State.RoundPreparation(
                        playersList = currentState.playersList,
                        settings = currentState.settings,
                        roundNumber = currentState.roundNumber,
                        cycleNumber = currentState.cycleNumber,
                        speakerIndex = currentState.speakerIndex,
                        listenerIndex = currentState.listenerIndex,
                        millisecondsLeft = currentState.settings.preparationTimeSeconds * 1000u,
                        restWords = currentState.restWords,
                        explanationScores = currentState.explanationScores,
                        guessingScores = currentState.guessingScores,
                        currentExplanationResults = emptyKoneList(),
                        timer = createTimer(
                            roundNumber = currentState.roundNumber,
                            startInstant = Clock.System.now(),
                            preparationTimeSeconds = currentState.settings.preparationTimeSeconds,
                            explanationTimeSeconds = currentState.settings.explanationTimeSeconds,
                            finalGuessTimeSeconds = currentState.settings.finalGuessTimeSeconds,
                        )
                    )
                } else State.RoundWaiting(
                    playersList = currentState.playersList,
                    settings = currentState.settings,
                    roundNumber = currentState.roundNumber,
                    cycleNumber = currentState.cycleNumber,
                    speakerIndex = currentState.speakerIndex,
                    listenerIndex = currentState.listenerIndex,
                    restWords = currentState.restWords,
                    explanationScores = currentState.explanationScores,
                    guessingScores = currentState.guessingScores,
                    speakerReady = currentState.speakerReady,
                    listenerReady = true,
                )
            
            is State.GameInitialisation,
            is State.RoundPreparation,
            is State.RoundExplanation,
            is State.RoundLastGuess,
            is State.RoundEditing,
            is State.GameResults,
                -> return Result.ListenerReadinessResult.InvalidState
        }
        _state.value = newState
        return Result.ListenerReadinessResult.Success
    }
    
    public fun wordExplanationState(wordState: WordExplanation.State): Result.WordExplanationStatementResult {
        val newState = when (val currentState = state.value) {
            is State.RoundExplanation ->
                if (currentState.restWords.isNotEmpty() && wordState == WordExplanation.State.Explained) {
                    val nextCurrentWord = currentState.restWords.random(Random)
                    val nextRestWords = currentState.restWords.filterTo(koneMutableSetOf()) { it != nextCurrentWord }
                    State.RoundExplanation(
                        playersList = currentState.playersList,
                        settings = currentState.settings,
                        roundNumber = currentState.roundNumber,
                        cycleNumber = currentState.cycleNumber,
                        speakerIndex = currentState.speakerIndex,
                        listenerIndex = currentState.listenerIndex,
                        millisecondsLeft = currentState.millisecondsLeft,
                        restWords = nextRestWords,
                        currentWord = nextCurrentWord,
                        explanationScores = currentState.explanationScores,
                        guessingScores = currentState.guessingScores,
                        currentExplanationResults = KoneList(currentState.currentExplanationResults.size + 1u) {
                            when (it) {
                                in currentState.currentExplanationResults.indices -> currentState.currentExplanationResults[it]
                                else -> WordExplanation(currentState.currentWord, wordState)
                            }
                        },
                        timer = currentState.timer,
                    )
                } else State.RoundEditing(
                    playersList = currentState.playersList,
                    settings = currentState.settings,
                    roundNumber = currentState.roundNumber,
                    cycleNumber = currentState.cycleNumber,
                    speakerIndex = currentState.speakerIndex,
                    listenerIndex = currentState.listenerIndex,
                    restWords = currentState.restWords,
                    explanationScores = currentState.explanationScores,
                    guessingScores = currentState.guessingScores,
                    currentExplanationResults = KoneList(currentState.currentExplanationResults.size + 1u) {
                        when (it) {
                            in currentState.currentExplanationResults.indices -> currentState.currentExplanationResults[it]
                            else -> WordExplanation(currentState.currentWord, wordState)
                        }
                    },
                )
            is State.RoundLastGuess ->
                State.RoundEditing(
                    playersList = currentState.playersList,
                    settings = currentState.settings,
                    roundNumber = currentState.roundNumber,
                    cycleNumber = currentState.cycleNumber,
                    speakerIndex = currentState.speakerIndex,
                    listenerIndex = currentState.listenerIndex,
                    restWords = currentState.restWords,
                    explanationScores = currentState.explanationScores,
                    guessingScores = currentState.guessingScores,
                    currentExplanationResults = KoneList(currentState.currentExplanationResults.size + 1u) {
                        when (it) {
                            in currentState.currentExplanationResults.indices -> currentState.currentExplanationResults[it]
                            else -> WordExplanation(currentState.currentWord, wordState)
                        }
                    },
                )
            
            is State.GameInitialisation,
            is State.RoundWaiting,
            is State.RoundPreparation,
            is State.RoundEditing,
            is State.GameResults,
                -> return Result.WordExplanationStatementResult.InvalidState
        }
        _state.value = newState
        return Result.WordExplanationStatementResult.Success
    }
    
    public fun updateWordsExplanationResults(newExplanationResults: KoneList<WordExplanation>) : Result.WordsExplanationResultsUpdateResult {
        val newState = when (val currentState = state.value) {
            is State.RoundEditing ->
                State.RoundEditing(
                    playersList = currentState.playersList,
                    settings = currentState.settings,
                    roundNumber = currentState.roundNumber,
                    cycleNumber = currentState.cycleNumber,
                    speakerIndex = currentState.speakerIndex,
                    listenerIndex = currentState.listenerIndex,
                    restWords = currentState.restWords,
                    explanationScores = currentState.explanationScores,
                    guessingScores = currentState.guessingScores,
                    currentExplanationResults = newExplanationResults,
                )
            
            is State.GameInitialisation,
            is State.RoundWaiting,
            is State.RoundPreparation,
            is State.RoundExplanation,
            is State.RoundLastGuess,
            is State.GameResults,
                -> return Result.WordsExplanationResultsUpdateResult.InvalidState
        }
        _state.value = newState
        return Result.WordsExplanationResultsUpdateResult.Success
    }
    
    public fun confirmWordsExplanationResults() : Result.WordsExplanationResultsConfirmationResult {
        val newState = when (val currentState = state.value) {
            is State.RoundEditing -> {
                val newRestWords = buildKoneSet<String> {
                    this += currentState.restWords
                    this += currentState.currentExplanationResults.filter { it.state == WordExplanation.State.NotExplained }.map { it.word }
                }
                val numberOfExplainedWords = currentState.currentExplanationResults.count { it.state == WordExplanation.State.Explained }
                val explanationScores = KoneList(currentState.playersList.size) { currentState.explanationScores[it] + if (it == currentState.speakerIndex) numberOfExplainedWords else 0u }
                val guessingScores = KoneList(currentState.playersList.size) { currentState.guessingScores[it] + if (it == currentState.listenerIndex) numberOfExplainedWords else 0u }
                val nextRoundNumber = currentState.roundNumber + 1u
                var nextCycleNumber = currentState.cycleNumber
                var nextSpeakerIndex = (currentState.speakerIndex + 1u) % currentState.playersList.size
                var nextListenerIndex = (currentState.listenerIndex + 1u) % currentState.playersList.size
                if (nextSpeakerIndex == 0u) {
                    nextListenerIndex++
                    if (nextListenerIndex == 0u) {
                        nextListenerIndex = 1u
                        nextCycleNumber++
                    }
                }
                if (
                    newRestWords.isEmpty() || when (val endCondition = currentState.settings.gameEndCondition) {
                        is GameEndCondition.Cycles -> endCondition.number == nextCycleNumber
                        is GameEndCondition.Words -> false
                    }
                ) State.GameResults(
                    playersList = currentState.playersList,
                    results = KoneSettableList(currentState.playersList.size) {
                        GameResult(
                            it,
                            explanationScores[it],
                            guessingScores[it],
                            explanationScores[it] + guessingScores[it],
                        )
                    }.apply { sortByDescending { it.sum } }
                )
                else State.RoundWaiting(
                    playersList = currentState.playersList,
                    settings = currentState.settings,
                    roundNumber = nextRoundNumber,
                    cycleNumber = nextCycleNumber,
                    speakerIndex = nextSpeakerIndex,
                    listenerIndex = nextListenerIndex,
                    restWords = newRestWords,
                    explanationScores = explanationScores,
                    guessingScores = guessingScores,
                    speakerReady = false,
                    listenerReady = false,
                )
            }
            is State.GameInitialisation,
            is State.RoundWaiting,
            is State.RoundPreparation,
            is State.RoundExplanation,
            is State.RoundLastGuess,
            is State.GameResults,
                -> return Result.WordsExplanationResultsConfirmationResult.InvalidState
        }
        _state.value = newState
        return Result.WordsExplanationResultsConfirmationResult.Success
    }
    
    public fun finishGame() : Result.GameFinishingResult {
        val newState = when (val currentState = state.value) {
            is State.RoundWaiting ->
                State.GameResults(
                    playersList = currentState.playersList,
                    results = KoneSettableList(currentState.playersList.size) {
                        GameResult(
                            player = it,
                            scoreExplained = currentState.explanationScores[it],
                            scoreGuessed = currentState.guessingScores[it],
                            sum = currentState.explanationScores[it] + currentState.guessingScores[it],
                        )
                    }.apply { sortByDescending { it.sum } }
                )
            is State.GameInitialisation,
            is State.RoundPreparation,
            is State.RoundExplanation,
            is State.RoundLastGuess,
            is State.RoundEditing,
            is State.GameResults,
                -> return Result.GameFinishingResult.InvalidState
        }
        _state.value = newState
        return Result.GameFinishingResult.Success
    }
    
    public companion object {
        @Suppress("FunctionName")
        public fun <P, WP: WordsProvider> FromInitialization(
            playersList: KoneList<P>,
            settingsBuilder: GameSettingsBuilder<WP>,
            coroutineScope: CoroutineScope,
            structuralMutex: Mutex,
            random: Random = DefaultRandom,
        ): GameStateMachine<P, WP> = GameStateMachine(
            initialState = State.GameInitialisation(
                playersList = playersList,
                settingsBuilder = settingsBuilder,
            ),
            coroutineScope = coroutineScope,
            structuralMutex = structuralMutex,
            random = random,
        )
        
        @Suppress("FunctionName")
        public fun <P, WP: WordsProvider> Initialized(
            playersList: KoneList<P>,
            settingsBuilder: GameSettingsBuilder<WP>,
            coroutineScope: CoroutineScope,
            structuralMutex: Mutex,
            random: Random = DefaultRandom,
        ): GameStateMachine<P, WP> = GameStateMachine(
            initialState = when (val wordsSource = settingsBuilder.wordsSource) {
                WordsSource.Players -> TODO()
                is WordsSource.Custom ->
                    State.RoundWaiting(
                        playersList = playersList,
                        settings = GameSettings(
                            preparationTimeSeconds = settingsBuilder.preparationTimeSeconds,
                            explanationTimeSeconds = settingsBuilder.explanationTimeSeconds,
                            finalGuessTimeSeconds = settingsBuilder.finalGuessTimeSeconds,
                            strictMode = settingsBuilder.strictMode,
                            gameEndCondition = when (settingsBuilder.gameEndConditionType) {
                                GameEndCondition.Type.Words -> GameEndCondition.Words(settingsBuilder.cachedEndConditionWordsNumber)
                                GameEndCondition.Type.Cycles -> GameEndCondition.Cycles(settingsBuilder.cachedEndConditionCyclesNumber)
                            },
                        ),
                        roundNumber = 0u,
                        cycleNumber = 0u,
                        speakerIndex = 0u,
                        listenerIndex = 1u,
                        restWords = when (settingsBuilder.gameEndConditionType) {
                            GameEndCondition.Type.Words -> wordsSource.provider.randomWords(settingsBuilder.cachedEndConditionWordsNumber)
                            GameEndCondition.Type.Cycles -> wordsSource.provider.allWords()
                        },
                        explanationScores = KoneList(playersList.size) { 0u },
                        guessingScores = KoneList(playersList.size) { 0u },
                        speakerReady = false,
                        listenerReady = false,
                    )
            },
            coroutineScope = coroutineScope,
            structuralMutex = structuralMutex,
            random = random,
        )
    }
}