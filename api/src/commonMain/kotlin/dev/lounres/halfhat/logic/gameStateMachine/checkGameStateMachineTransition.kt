package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.automata.CheckResult
import dev.lounres.kone.collections.array.KoneMutableUIntArray
import dev.lounres.kone.collections.array.KoneUIntArray
import dev.lounres.kone.collections.array.fill
import dev.lounres.kone.collections.array.toKoneUIntArray
import dev.lounres.kone.collections.iterables.isEmpty
import dev.lounres.kone.collections.iterables.isNotEmpty
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.empty
import dev.lounres.kone.collections.list.generate
import dev.lounres.kone.collections.list.indices
import dev.lounres.kone.collections.list.toKoneSettableList
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.map.build
import dev.lounres.kone.collections.map.of
import dev.lounres.kone.collections.map.setAllFrom
import dev.lounres.kone.collections.set.KoneMutableSet
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.addAllFrom
import dev.lounres.kone.collections.set.build
import dev.lounres.kone.collections.set.of
import dev.lounres.kone.collections.utils.*
import dev.lounres.kone.repeat
import dev.lounres.kone.scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration


@PublishedApi
internal inline fun <P, WPID, NoWordsProviderReason, GSM> GSM.timer(
    coroutineScope: CoroutineScope,
    timerDelayDuration: Duration,
    roundNumber: UInt,
    crossinline moveState: suspend GSM.(GameStateMachine.Transition<P, WPID, NoWordsProviderReason>) -> Unit,
) {
    coroutineScope.launch {
        var isActive = true
        while (isActive) {
            moveState(
                GameStateMachine.Transition.UpdateRoundInfo(
                    stopTimer = { isActive = false },
                    roundNumber = roundNumber
                )
            )
            delay(timerDelayDuration)
        }
    }
}

@PublishedApi
internal data class ScheduledPair(
    val speakerIndex: UInt,
    val listenerIndex: UInt,
)

@PublishedApi
internal fun nextScheduledPairFor(
    playersNumber: UInt,
    pair: ScheduledPair
): ScheduledPair {
    val nextSpeakerIndex = (pair.speakerIndex + 1u) % playersNumber
    var nextListenerIndex = (pair.listenerIndex + 1u) % playersNumber
    if (nextSpeakerIndex == 0u) {
        nextListenerIndex = (nextListenerIndex + 1u) % playersNumber
        if (nextListenerIndex == 0u) {
            nextListenerIndex = 1u
        }
    }
    return ScheduledPair(
        speakerIndex = nextSpeakerIndex,
        listenerIndex = nextListenerIndex,
    )
}

@PublishedApi
internal data class Schedule(
    val playersRoundsBeforeSpeaking: KoneUIntArray,
    val playersRoundsBeforeListening: KoneUIntArray,
)

@PublishedApi
internal fun scheduleFor(
    playersNumber: UInt,
    pair: ScheduledPair
): Schedule {
    val playersRoundsBeforeSpeaking = KoneMutableUIntArray.fill(playersNumber)
    val playersRoundsBeforeListening = KoneMutableUIntArray.fill(playersNumber)
    
    var currentPair = pair
    repeat(playersNumber * 2u) {
        currentPair = nextScheduledPairFor(playersNumber, currentPair)
        if (playersRoundsBeforeSpeaking[currentPair.speakerIndex] == 0u)
            playersRoundsBeforeSpeaking[currentPair.speakerIndex] = it + 1u
        if (playersRoundsBeforeListening[currentPair.listenerIndex] == 0u)
            playersRoundsBeforeListening[currentPair.listenerIndex] = it + 1u
    }
    
    return Schedule(
        playersRoundsBeforeSpeaking = playersRoundsBeforeSpeaking.toKoneUIntArray(),
        playersRoundsBeforeListening = playersRoundsBeforeListening.toKoneUIntArray(),
    )
}

// TODO: Think about delay after speaker and listener readiness confirmation and before preparation countdown
@PublishedApi
internal suspend inline fun <P, WPID, NoWordsProviderReason, GSM> GSM.checkGameStateMachineTransition(
    coroutineScope: CoroutineScope,
    random: Random = DefaultRandom,
    timerDelayDuration: Duration,
    crossinline moveState: suspend GSM.(GameStateMachine.Transition<P, WPID, NoWordsProviderReason>) -> Unit,
    previousState: GameStateMachine.State<P, WPID>,
    transition: GameStateMachine.Transition<P, WPID, NoWordsProviderReason>,
): CheckResult<GameStateMachine.State<P, WPID>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> =
    when (previousState) {
        is GameStateMachine.State.GameInitialisation ->
            when (transition) {
                is GameStateMachine.Transition.UpdateGameSettings ->
                    CheckResult.Success(
                        GameStateMachine.State.GameInitialisation(
                            playersList = transition.playersList,
                            settingsBuilder = transition.settingsBuilder
                        )
                    )
                is GameStateMachine.Transition.InitialiseGame -> scope {
                    val playersList = previousState.playersList
                    val settingsBuilder = previousState.settingsBuilder

                    if (playersList.size < 2u) return@scope CheckResult.Failure(GameStateMachine.NoNextStateReason.NotEnoughPlayersForInitialization)

                    when (val wordsSource = settingsBuilder.wordsSource) {
                        GameStateMachine.WordsSource.Players ->
                            CheckResult.Success(
                                GameStateMachine.State.GameInitialised.PlayersWordsCollection(
                                    playersList = playersList,
                                    settings = settingsBuilder.build(),
                                    playersWords = KoneList.generate(playersList.size) { null }
                                )
                            )
                        is GameStateMachine.WordsSource.Custom -> {
                            val wordsProviderOrReason = transition.wordsProviderRegistry.getWordsProvider(wordsSource.providerId)

                            when (wordsProviderOrReason) {
                                is GameStateMachine.WordsProviderRegistry.WordsProviderOrReason.Failure ->
                                    CheckResult.Failure(GameStateMachine.NoNextStateReason.NoWordsProvider(wordsProviderOrReason.reason))
                                is GameStateMachine.WordsProviderRegistry.WordsProviderOrReason.Success -> {
                                    val restWords = when (settingsBuilder.gameEndConditionType) {
                                        GameStateMachine.GameEndCondition.Type.Words -> wordsProviderOrReason.result.randomWords(settingsBuilder.cachedEndConditionWordsNumber)
                                        GameStateMachine.GameEndCondition.Type.Cycles -> wordsProviderOrReason.result.allWords()
                                    }
                                    val nextPair = nextScheduledPairFor(playersList.size, ScheduledPair(0u, 1u))
                                    val schedule = scheduleFor(playersList.size, ScheduledPair(0u, 1u))
                                    CheckResult.Success(
                                        GameStateMachine.State.GameInitialised.Round.RoundWaiting(
                                            playersList = playersList,
                                            settings = settingsBuilder.build(),
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
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                is GameStateMachine.Transition.SubmitPlayerWords -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitPlayerWordsNotDuringPlayersWordsCollection)
                GameStateMachine.Transition.SpeakerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerReadinessNotDuringRoundWaiting)
                GameStateMachine.Transition.ListenerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetListenerReadinessNotDuringRoundWaiting)
                GameStateMachine.Transition.SpeakerAndListenerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting)
                is GameStateMachine.Transition.UpdateRoundInfo -> {
                    transition.stopTimer()
                    CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                }
                is GameStateMachine.Transition.WordExplanationState -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess)
                is GameStateMachine.Transition.UpdateWordsExplanationResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateWordExplanationResultsNotDuringRoundEditing)
                GameStateMachine.Transition.ConfirmWordsExplanationResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotConfirmWordExplanationResultsNotDuringRoundEditing)
                GameStateMachine.Transition.FinishGame -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotFinishGameNotDuringRoundWaiting)
            }
        is GameStateMachine.State.GameInitialised ->
            checkGameStateMachineTransitionForGameInitialisedState(
                coroutineScope = coroutineScope,
                random = random,
                timerDelayDuration = timerDelayDuration,
                moveState = moveState,
                previousState = previousState,
                transition = transition,
            )
    }

@PublishedApi
internal inline fun <P, WPID, NoWordsProviderReason, GSM> GSM.checkGameStateMachineTransitionForGameInitialisedState(
    coroutineScope: CoroutineScope,
    random: Random = DefaultRandom,
    timerDelayDuration: Duration,
    crossinline moveState: suspend GSM.(GameStateMachine.Transition<P, WPID, NoWordsProviderReason>) -> Unit,
    previousState: GameStateMachine.State.GameInitialised<P, WPID>,
    transition: GameStateMachine.Transition<P, WPID, NoWordsProviderReason>,
): CheckResult<GameStateMachine.State.GameInitialised<P, WPID>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> =
    when (previousState) {
        is GameStateMachine.State.GameInitialised.PlayersWordsCollection ->
            when (transition) {
                is GameStateMachine.Transition.UpdateGameSettings -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateGameSettingsAfterInitialization)
                is GameStateMachine.Transition.InitialiseGame -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotInitializeGameAfterInitialization)
                is GameStateMachine.Transition.SubmitPlayerWords ->
                    if (previousState.playersWords[transition.playerIndex] != null)
                        CheckResult.Failure(GameStateMachine.NoNextStateReason.PlayerAlreadySubmittedWords)
                    else {
                        val newPlayersWords = previousState.playersWords.toKoneSettableList()
                        newPlayersWords[transition.playerIndex] = transition.playerWords
                        if (newPlayersWords.all { it != null }) {
                            val restWords = KoneSet.build {
                                for (words in newPlayersWords) addAllFrom(words!!)
                            }
                            val nextPair = nextScheduledPairFor(previousState.playersList.size, ScheduledPair(0u, 1u))
                            val schedule = scheduleFor(previousState.playersList.size, ScheduledPair(0u, 1u))
                            CheckResult.Success(
                                GameStateMachine.State.GameInitialised.Round.RoundWaiting(
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
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
                                    explanationScores = KoneList.generate(previousState.playersList.size) { 0u },
                                    guessingScores = KoneList.generate(previousState.playersList.size) { 0u },
                                    wordsStatistic = KoneMap.of(),
                                    speakerReady = false,
                                    listenerReady = false,
                                )
                            )
                        } else
                            CheckResult.Success(
                                GameStateMachine.State.GameInitialised.PlayersWordsCollection(
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
                                    playersWords = newPlayersWords,
                                )
                            )
                    }
                GameStateMachine.Transition.SpeakerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerReadinessNotDuringRoundWaiting)
                GameStateMachine.Transition.ListenerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetListenerReadinessNotDuringRoundWaiting)
                GameStateMachine.Transition.SpeakerAndListenerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting)
                is GameStateMachine.Transition.UpdateRoundInfo -> {
                    transition.stopTimer()
                    CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                }
                is GameStateMachine.Transition.WordExplanationState -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess)
                is GameStateMachine.Transition.UpdateWordsExplanationResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateWordExplanationResultsNotDuringRoundEditing)
                GameStateMachine.Transition.ConfirmWordsExplanationResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotConfirmWordExplanationResultsNotDuringRoundEditing)
                GameStateMachine.Transition.FinishGame -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotFinishGameNotDuringRoundWaiting)
            }
        is GameStateMachine.State.GameInitialised.Round.RoundWaiting ->
            when (transition) {
                is GameStateMachine.Transition.UpdateGameSettings -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateGameSettingsAfterInitialization)
                is GameStateMachine.Transition.InitialiseGame -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotInitializeGameAfterInitialization)
                is GameStateMachine.Transition.SubmitPlayerWords -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitPlayerWordsNotDuringPlayersWordsCollection)
                GameStateMachine.Transition.SpeakerReady ->
                    if (previousState.listenerReady) {
                        timer(
                            coroutineScope = coroutineScope,
                            timerDelayDuration = timerDelayDuration,
                            roundNumber = previousState.roundNumber,
                            moveState = moveState,
                        )
                        CheckResult.Success(
                            GameStateMachine.State.GameInitialised.Round.RoundPreparation(
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                initialWordsNumber = previousState.initialWordsNumber,
                                roundNumber = previousState.roundNumber,
                                cycleNumber = previousState.cycleNumber,
                                speakerIndex = previousState.speakerIndex,
                                listenerIndex = previousState.listenerIndex,
                                nextSpeakerIndex = previousState.nextSpeakerIndex,
                                nextListenerIndex = previousState.nextListenerIndex,
                                playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                startInstant = Clock.System.now(),
                                millisecondsLeft = previousState.settings.preparationTimeSeconds * 1000u,
                                restWords = previousState.restWords,
                                explanationScores = previousState.explanationScores,
                                guessingScores = previousState.guessingScores,
                                wordsStatistic = previousState.wordsStatistic,
                                currentExplanationResults = KoneList.empty(),
                            )
                        )
                    }
                    else
                        CheckResult.Success(
                            GameStateMachine.State.GameInitialised.Round.RoundWaiting(
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                initialWordsNumber = previousState.initialWordsNumber,
                                roundNumber = previousState.roundNumber,
                                cycleNumber = previousState.cycleNumber,
                                speakerIndex = previousState.speakerIndex,
                                listenerIndex = previousState.listenerIndex,
                                nextSpeakerIndex = previousState.nextSpeakerIndex,
                                nextListenerIndex = previousState.nextListenerIndex,
                                playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                restWords = previousState.restWords,
                                explanationScores = previousState.explanationScores,
                                guessingScores = previousState.guessingScores,
                                wordsStatistic = previousState.wordsStatistic,
                                speakerReady = true,
                                listenerReady = previousState.listenerReady,
                            )
                        )
                GameStateMachine.Transition.ListenerReady ->
                    if (previousState.speakerReady) {
                        timer(
                            coroutineScope = coroutineScope,
                            timerDelayDuration = timerDelayDuration,
                            roundNumber = previousState.roundNumber,
                            moveState = moveState,
                        )
                        CheckResult.Success(
                            GameStateMachine.State.GameInitialised.Round.RoundPreparation(
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                initialWordsNumber = previousState.initialWordsNumber,
                                roundNumber = previousState.roundNumber,
                                cycleNumber = previousState.cycleNumber,
                                speakerIndex = previousState.speakerIndex,
                                listenerIndex = previousState.listenerIndex,
                                nextSpeakerIndex = previousState.nextSpeakerIndex,
                                nextListenerIndex = previousState.nextListenerIndex,
                                playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                startInstant = Clock.System.now(),
                                millisecondsLeft = previousState.settings.preparationTimeSeconds * 1000u,
                                restWords = previousState.restWords,
                                explanationScores = previousState.explanationScores,
                                guessingScores = previousState.guessingScores,
                                wordsStatistic = previousState.wordsStatistic,
                                currentExplanationResults = KoneList.empty(),
                            )
                        )
                    }
                    else
                        CheckResult.Success(
                            GameStateMachine.State.GameInitialised.Round.RoundWaiting(
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                initialWordsNumber = previousState.initialWordsNumber,
                                roundNumber = previousState.roundNumber,
                                cycleNumber = previousState.cycleNumber,
                                speakerIndex = previousState.speakerIndex,
                                listenerIndex = previousState.listenerIndex,
                                nextSpeakerIndex = previousState.nextSpeakerIndex,
                                nextListenerIndex = previousState.nextListenerIndex,
                                playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                restWords = previousState.restWords,
                                explanationScores = previousState.explanationScores,
                                guessingScores = previousState.guessingScores,
                                wordsStatistic = previousState.wordsStatistic,
                                speakerReady = previousState.speakerReady,
                                listenerReady = true,
                            )
                        )
                GameStateMachine.Transition.SpeakerAndListenerReady -> {
                    timer(
                        coroutineScope = coroutineScope,
                        timerDelayDuration = timerDelayDuration,
                        roundNumber = previousState.roundNumber,
                        moveState = moveState,
                    )
                    CheckResult.Success(
                        GameStateMachine.State.GameInitialised.Round.RoundPreparation(
                            playersList = previousState.playersList,
                            settings = previousState.settings,
                            initialWordsNumber = previousState.initialWordsNumber,
                            roundNumber = previousState.roundNumber,
                            cycleNumber = previousState.cycleNumber,
                            speakerIndex = previousState.speakerIndex,
                            listenerIndex = previousState.listenerIndex,
                            nextSpeakerIndex = previousState.nextSpeakerIndex,
                            nextListenerIndex = previousState.nextListenerIndex,
                            playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                            playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                            startInstant = Clock.System.now(),
                            millisecondsLeft = previousState.settings.preparationTimeSeconds * 1000u,
                            restWords = previousState.restWords,
                            explanationScores = previousState.explanationScores,
                            guessingScores = previousState.guessingScores,
                            wordsStatistic = previousState.wordsStatistic,
                            currentExplanationResults = KoneList.empty(),
                        )
                    )
                }
                is GameStateMachine.Transition.UpdateRoundInfo -> {
                    transition.stopTimer()
                    CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                }
                is GameStateMachine.Transition.WordExplanationState -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess)
                is GameStateMachine.Transition.UpdateWordsExplanationResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateWordExplanationResultsNotDuringRoundEditing)
                GameStateMachine.Transition.ConfirmWordsExplanationResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotConfirmWordExplanationResultsNotDuringRoundEditing)
                GameStateMachine.Transition.FinishGame ->
                    CheckResult.Success(
                        GameStateMachine.State.GameInitialised.GameResults(
                            playersList = previousState.playersList,
                            settings = previousState.settings,
                            explanationScores = previousState.explanationScores,
                            guessingScores = previousState.guessingScores,
                            wordsStatistic = previousState.wordsStatistic,
                        )
                    )
            }
        is GameStateMachine.State.GameInitialised.Round.RoundPreparation ->
            when (transition) {
                is GameStateMachine.Transition.UpdateGameSettings -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateGameSettingsAfterInitialization)
                is GameStateMachine.Transition.InitialiseGame -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotInitializeGameAfterInitialization)
                is GameStateMachine.Transition.SubmitPlayerWords -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitPlayerWordsNotDuringPlayersWordsCollection)
                GameStateMachine.Transition.SpeakerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerReadinessNotDuringRoundWaiting)
                GameStateMachine.Transition.ListenerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetListenerReadinessNotDuringRoundWaiting)
                GameStateMachine.Transition.SpeakerAndListenerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting)
                is GameStateMachine.Transition.UpdateRoundInfo -> {
                    val currentInstant = Clock.System.now()
                    val spentTimeMilliseconds = (currentInstant - previousState.startInstant).inWholeMilliseconds.toUInt()
                    val preparationTimeSeconds = previousState.settings.preparationTimeSeconds
                    val explanationTimeSeconds = previousState.settings.explanationTimeSeconds
                    val finalGuessTimeSeconds = previousState.settings.finalGuessTimeSeconds
                    when {
                        previousState.roundNumber != transition.roundNumber -> {
                            transition.stopTimer()
                            CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                        }
                        spentTimeMilliseconds < preparationTimeSeconds * 1000u ->
                            CheckResult.Success(
                                GameStateMachine.State.GameInitialised.Round.RoundPreparation(
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
                                    initialWordsNumber = previousState.initialWordsNumber,
                                    roundNumber = previousState.roundNumber,
                                    cycleNumber = previousState.cycleNumber,
                                    speakerIndex = previousState.speakerIndex,
                                    listenerIndex = previousState.listenerIndex,
                                    nextSpeakerIndex = previousState.nextSpeakerIndex,
                                    nextListenerIndex = previousState.nextListenerIndex,
                                    playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                    playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                    startInstant = previousState.startInstant,
                                    millisecondsLeft = preparationTimeSeconds * 1000u - spentTimeMilliseconds,
                                    restWords = previousState.restWords,
                                    explanationScores = previousState.explanationScores,
                                    guessingScores = previousState.guessingScores,
                                    wordsStatistic = previousState.wordsStatistic,
                                    currentExplanationResults = previousState.currentExplanationResults,
                                )
                            )

                        spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds) * 1000u -> {
                            val currentWord = previousState.restWords.random(random)
                            val restWords =
                                previousState.restWords.filterTo(KoneMutableSet.of()) { it != currentWord }
                            CheckResult.Success(
                                GameStateMachine.State.GameInitialised.Round.RoundExplanation(
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
                                    initialWordsNumber = previousState.initialWordsNumber,
                                    roundNumber = previousState.roundNumber,
                                    cycleNumber = previousState.cycleNumber,
                                    speakerIndex = previousState.speakerIndex,
                                    listenerIndex = previousState.listenerIndex,
                                    nextSpeakerIndex = previousState.nextSpeakerIndex,
                                    nextListenerIndex = previousState.nextListenerIndex,
                                    playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                    playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                    startInstant = previousState.startInstant,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = restWords,
                                    currentWord = currentWord,
                                    explanationScores = previousState.explanationScores,
                                    guessingScores = previousState.guessingScores,
                                    wordsStatistic = previousState.wordsStatistic,
                                    wordExplanationStart = currentInstant,
                                    currentExplanationResults = previousState.currentExplanationResults,
                                )
                            )
                        }

                        spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u -> {
                            val currentWord = previousState.restWords.random(random)
                            val restWords =
                                previousState.restWords.filterTo(KoneMutableSet.of()) { it != currentWord }
                            CheckResult.Success(
                                GameStateMachine.State.GameInitialised.Round.RoundLastGuess(
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
                                    initialWordsNumber = previousState.initialWordsNumber,
                                    roundNumber = previousState.roundNumber,
                                    cycleNumber = previousState.cycleNumber,
                                    speakerIndex = previousState.speakerIndex,
                                    listenerIndex = previousState.listenerIndex,
                                    nextSpeakerIndex = previousState.nextSpeakerIndex,
                                    nextListenerIndex = previousState.nextListenerIndex,
                                    playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                    playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                    startInstant = previousState.startInstant,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = restWords,
                                    currentWord = currentWord,
                                    explanationScores = previousState.explanationScores,
                                    guessingScores = previousState.guessingScores,
                                    wordsStatistic = previousState.wordsStatistic,
                                    wordExplanationStart = currentInstant,
                                    currentExplanationResults = previousState.currentExplanationResults,
                                )
                            )
                        }

                        else -> {
                            transition.stopTimer()

                            if (previousState.settings.strictMode)
                                CheckResult.Success(
                                    GameStateMachine.State.GameInitialised.Round.RoundEditing(
                                        playersList = previousState.playersList,
                                        settings = previousState.settings,
                                        initialWordsNumber = previousState.initialWordsNumber,
                                        roundNumber = previousState.roundNumber,
                                        cycleNumber = previousState.cycleNumber,
                                        speakerIndex = previousState.speakerIndex,
                                        listenerIndex = previousState.listenerIndex,
                                        nextSpeakerIndex = previousState.nextSpeakerIndex,
                                        nextListenerIndex = previousState.nextListenerIndex,
                                        playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                        playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                        restWords = previousState.restWords,
                                        explanationScores = previousState.explanationScores,
                                        guessingScores = previousState.guessingScores,
                                        wordsStatistic = previousState.wordsStatistic,
                                        currentExplanationResults = previousState.currentExplanationResults,
                                    )
                                )
                            else {
                                val currentWord = previousState.restWords.random(random)
                                val restWords =
                                    previousState.restWords.filterTo(KoneMutableSet.of()) { it != currentWord }
                                CheckResult.Success(
                                    GameStateMachine.State.GameInitialised.Round.RoundLastGuess(
                                        playersList = previousState.playersList,
                                        settings = previousState.settings,
                                        initialWordsNumber = previousState.initialWordsNumber,
                                        roundNumber = previousState.roundNumber,
                                        cycleNumber = previousState.cycleNumber,
                                        speakerIndex = previousState.speakerIndex,
                                        listenerIndex = previousState.listenerIndex,
                                        nextSpeakerIndex = previousState.nextSpeakerIndex,
                                        nextListenerIndex = previousState.nextListenerIndex,
                                        playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                        playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                        startInstant = previousState.startInstant,
                                        millisecondsLeft = 0u,
                                        restWords = restWords,
                                        currentWord = currentWord,
                                        explanationScores = previousState.explanationScores,
                                        guessingScores = previousState.guessingScores,
                                        wordsStatistic = previousState.wordsStatistic,
                                        wordExplanationStart = currentInstant,
                                        currentExplanationResults = previousState.currentExplanationResults,
                                    )
                                )
                            }
                        }
                    }
                }
                is GameStateMachine.Transition.WordExplanationState -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess)
                is GameStateMachine.Transition.UpdateWordsExplanationResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateWordExplanationResultsNotDuringRoundEditing)
                GameStateMachine.Transition.ConfirmWordsExplanationResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotConfirmWordExplanationResultsNotDuringRoundEditing)
                GameStateMachine.Transition.FinishGame -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotFinishGameNotDuringRoundWaiting)
            }
        is GameStateMachine.State.GameInitialised.Round.RoundExplanation ->
            when (transition) {
                is GameStateMachine.Transition.UpdateGameSettings -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateGameSettingsAfterInitialization)
                is GameStateMachine.Transition.InitialiseGame -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotInitializeGameAfterInitialization)
                is GameStateMachine.Transition.SubmitPlayerWords -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitPlayerWordsNotDuringPlayersWordsCollection)
                GameStateMachine.Transition.SpeakerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerReadinessNotDuringRoundWaiting)
                GameStateMachine.Transition.ListenerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetListenerReadinessNotDuringRoundWaiting)
                GameStateMachine.Transition.SpeakerAndListenerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting)
                is GameStateMachine.Transition.UpdateRoundInfo -> {
                    val currentInstant = Clock.System.now()
                    val spentTimeMilliseconds = (currentInstant - previousState.startInstant).inWholeMilliseconds.toUInt()
                    val preparationTimeSeconds = previousState.settings.preparationTimeSeconds
                    val explanationTimeSeconds = previousState.settings.explanationTimeSeconds
                    val finalGuessTimeSeconds = previousState.settings.finalGuessTimeSeconds
                    when {
                        previousState.roundNumber != transition.roundNumber -> {
                            transition.stopTimer()
                            CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                        }
                        spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds) * 1000u ->
                            CheckResult.Success(
                                GameStateMachine.State.GameInitialised.Round.RoundExplanation(
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
                                    initialWordsNumber = previousState.initialWordsNumber,
                                    roundNumber = previousState.roundNumber,
                                    cycleNumber = previousState.cycleNumber,
                                    speakerIndex = previousState.speakerIndex,
                                    listenerIndex = previousState.listenerIndex,
                                    nextSpeakerIndex = previousState.nextSpeakerIndex,
                                    nextListenerIndex = previousState.nextListenerIndex,
                                    playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                    playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                    startInstant = previousState.startInstant,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = previousState.restWords,
                                    currentWord = previousState.currentWord,
                                    explanationScores = previousState.explanationScores,
                                    guessingScores = previousState.guessingScores,
                                    wordsStatistic = previousState.wordsStatistic,
                                    wordExplanationStart = previousState.wordExplanationStart,
                                    currentExplanationResults = previousState.currentExplanationResults,
                                )
                            )

                        spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u ->
                            CheckResult.Success(
                                GameStateMachine.State.GameInitialised.Round.RoundLastGuess(
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
                                    initialWordsNumber = previousState.initialWordsNumber,
                                    roundNumber = previousState.roundNumber,
                                    cycleNumber = previousState.cycleNumber,
                                    speakerIndex = previousState.speakerIndex,
                                    listenerIndex = previousState.listenerIndex,
                                    nextSpeakerIndex = previousState.nextSpeakerIndex,
                                    nextListenerIndex = previousState.nextListenerIndex,
                                    playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                    playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                    startInstant = previousState.startInstant,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = previousState.restWords,
                                    currentWord = previousState.currentWord,
                                    explanationScores = previousState.explanationScores,
                                    guessingScores = previousState.guessingScores,
                                    wordsStatistic = previousState.wordsStatistic,
                                    wordExplanationStart = previousState.wordExplanationStart,
                                    currentExplanationResults = previousState.currentExplanationResults,
                                )
                            )

                        else -> {
                            transition.stopTimer()

                            if (previousState.settings.strictMode)
                                CheckResult.Success(
                                    GameStateMachine.State.GameInitialised.Round.RoundEditing(
                                        playersList = previousState.playersList,
                                        settings = previousState.settings,
                                        initialWordsNumber = previousState.initialWordsNumber,
                                        roundNumber = previousState.roundNumber,
                                        cycleNumber = previousState.cycleNumber,
                                        speakerIndex = previousState.speakerIndex,
                                        listenerIndex = previousState.listenerIndex,
                                        nextSpeakerIndex = previousState.nextSpeakerIndex,
                                        nextListenerIndex = previousState.nextListenerIndex,
                                        playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                        playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                        restWords = previousState.restWords,
                                        explanationScores = previousState.explanationScores,
                                        guessingScores = previousState.guessingScores,
                                        wordsStatistic = KoneMap.build {
                                            setAllFrom(previousState.wordsStatistic)
                                            setOrChange(
                                                key = previousState.currentWord,
                                                valueOnSet = {
                                                    GameStateMachine.WordStatistic(
                                                        currentInstant - previousState.wordExplanationStart,
                                                        GameStateMachine.WordStatistic.State.InProgress,
                                                    )
                                                },
                                                transformOnChange = {
                                                    GameStateMachine.WordStatistic(
                                                        currentInstant - previousState.wordExplanationStart + it.spentTime,
                                                        it.state,
                                                    )
                                                }
                                            )
                                        },
                                        currentExplanationResults = KoneList.generate(previousState.currentExplanationResults.size + 1u) {
                                            when (it) {
                                                in previousState.currentExplanationResults.indices -> previousState.currentExplanationResults[it]
                                                else -> GameStateMachine.WordExplanation(
                                                    previousState.currentWord,
                                                    GameStateMachine.WordExplanation.State.NotExplained
                                                )
                                            }
                                        },
                                    )
                                )
                            else
                                CheckResult.Success(
                                    GameStateMachine.State.GameInitialised.Round.RoundLastGuess(
                                        playersList = previousState.playersList,
                                        settings = previousState.settings,
                                        initialWordsNumber = previousState.initialWordsNumber,
                                        roundNumber = previousState.roundNumber,
                                        cycleNumber = previousState.cycleNumber,
                                        speakerIndex = previousState.speakerIndex,
                                        listenerIndex = previousState.listenerIndex,
                                        nextSpeakerIndex = previousState.nextSpeakerIndex,
                                        nextListenerIndex = previousState.nextListenerIndex,
                                        playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                        playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                        startInstant = previousState.startInstant,
                                        millisecondsLeft = 0u,
                                        restWords = previousState.restWords,
                                        currentWord = previousState.currentWord,
                                        explanationScores = previousState.explanationScores,
                                        guessingScores = previousState.guessingScores,
                                        wordsStatistic = previousState.wordsStatistic,
                                        wordExplanationStart = previousState.wordExplanationStart,
                                        currentExplanationResults = previousState.currentExplanationResults,
                                    )
                                )
                        }
                    }
                }
                is GameStateMachine.Transition.WordExplanationState -> {
                    val currentInstant = Clock.System.now()
                    if (previousState.restWords.isNotEmpty() && transition.wordState == GameStateMachine.WordExplanation.State.Explained) {
                        val nextCurrentWord = previousState.restWords.random(Random)
                        val nextRestWords =
                            previousState.restWords.filterTo(KoneMutableSet.of()) { it != nextCurrentWord }
                        CheckResult.Success(
                            GameStateMachine.State.GameInitialised.Round.RoundExplanation(
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                initialWordsNumber = previousState.initialWordsNumber,
                                roundNumber = previousState.roundNumber,
                                cycleNumber = previousState.cycleNumber,
                                speakerIndex = previousState.speakerIndex,
                                listenerIndex = previousState.listenerIndex,
                                nextSpeakerIndex = previousState.nextSpeakerIndex,
                                nextListenerIndex = previousState.nextListenerIndex,
                                playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                startInstant = previousState.startInstant,
                                millisecondsLeft = previousState.millisecondsLeft,
                                restWords = nextRestWords,
                                currentWord = nextCurrentWord,
                                explanationScores = previousState.explanationScores,
                                guessingScores = previousState.guessingScores,
                                wordsStatistic = KoneMap.build {
                                    setAllFrom(previousState.wordsStatistic)
                                    setOrChange(
                                        key = previousState.currentWord,
                                        valueOnSet = {
                                            GameStateMachine.WordStatistic(
                                                currentInstant - previousState.wordExplanationStart,
                                                GameStateMachine.WordStatistic.State.InProgress,
                                            )
                                        },
                                        transformOnChange = {
                                            GameStateMachine.WordStatistic(
                                                currentInstant - previousState.wordExplanationStart + it.spentTime,
                                                it.state,
                                            )
                                        }
                                    )
                                },
                                wordExplanationStart = currentInstant,
                                currentExplanationResults = KoneList.generate(previousState.currentExplanationResults.size + 1u) {
                                    when (it) {
                                        in previousState.currentExplanationResults.indices -> previousState.currentExplanationResults[it]
                                        else -> GameStateMachine.WordExplanation(
                                            previousState.currentWord,
                                            transition.wordState
                                        )
                                    }
                                },
                            )
                        )
                    } else {
                        CheckResult.Success(
                            GameStateMachine.State.GameInitialised.Round.RoundEditing(
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                initialWordsNumber = previousState.initialWordsNumber,
                                roundNumber = previousState.roundNumber,
                                cycleNumber = previousState.cycleNumber,
                                speakerIndex = previousState.speakerIndex,
                                listenerIndex = previousState.listenerIndex,
                                nextSpeakerIndex = previousState.nextSpeakerIndex,
                                nextListenerIndex = previousState.nextListenerIndex,
                                playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                restWords = previousState.restWords,
                                explanationScores = previousState.explanationScores,
                                guessingScores = previousState.guessingScores,
                                wordsStatistic = KoneMap.build {
                                    setAllFrom(previousState.wordsStatistic)
                                    setOrChange(
                                        key = previousState.currentWord,
                                        valueOnSet = {
                                            GameStateMachine.WordStatistic(
                                                currentInstant - previousState.wordExplanationStart,
                                                GameStateMachine.WordStatistic.State.InProgress,
                                            )
                                        },
                                        transformOnChange = {
                                            GameStateMachine.WordStatistic(
                                                currentInstant - previousState.wordExplanationStart + it.spentTime,
                                                it.state,
                                            )
                                        }
                                    )
                                },
                                currentExplanationResults = KoneList.generate(previousState.currentExplanationResults.size + 1u) {
                                    when (it) {
                                        in previousState.currentExplanationResults.indices -> previousState.currentExplanationResults[it]
                                        else -> GameStateMachine.WordExplanation(
                                            previousState.currentWord,
                                            transition.wordState
                                        )
                                    }
                                },
                            )
                        )
                    }
                }
                is GameStateMachine.Transition.UpdateWordsExplanationResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateWordExplanationResultsNotDuringRoundEditing)
                GameStateMachine.Transition.ConfirmWordsExplanationResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotConfirmWordExplanationResultsNotDuringRoundEditing)
                GameStateMachine.Transition.FinishGame -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotFinishGameNotDuringRoundWaiting)
            }
        is GameStateMachine.State.GameInitialised.Round.RoundLastGuess ->
            when (transition) {
                is GameStateMachine.Transition.UpdateGameSettings -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateGameSettingsAfterInitialization)
                is GameStateMachine.Transition.InitialiseGame -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotInitializeGameAfterInitialization)
                is GameStateMachine.Transition.SubmitPlayerWords -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitPlayerWordsNotDuringPlayersWordsCollection)
                GameStateMachine.Transition.SpeakerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerReadinessNotDuringRoundWaiting)
                GameStateMachine.Transition.ListenerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetListenerReadinessNotDuringRoundWaiting)
                GameStateMachine.Transition.SpeakerAndListenerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting)
                is GameStateMachine.Transition.UpdateRoundInfo -> {
                    val currentInstant = Clock.System.now()
                    val spentTimeMilliseconds = (currentInstant - previousState.startInstant).inWholeMilliseconds.toUInt()
                    val preparationTimeSeconds = previousState.settings.preparationTimeSeconds
                    val explanationTimeSeconds = previousState.settings.explanationTimeSeconds
                    val finalGuessTimeSeconds = previousState.settings.finalGuessTimeSeconds
                    when {
                        previousState.roundNumber != transition.roundNumber -> {
                            transition.stopTimer()
                            CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                        }
                        spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u ->
                            CheckResult.Success(
                                GameStateMachine.State.GameInitialised.Round.RoundLastGuess(
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
                                    initialWordsNumber = previousState.initialWordsNumber,
                                    roundNumber = previousState.roundNumber,
                                    cycleNumber = previousState.cycleNumber,
                                    speakerIndex = previousState.speakerIndex,
                                    listenerIndex = previousState.listenerIndex,
                                    nextSpeakerIndex = previousState.nextSpeakerIndex,
                                    nextListenerIndex = previousState.nextListenerIndex,
                                    playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                    playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                    startInstant = previousState.startInstant,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = previousState.restWords,
                                    currentWord = previousState.currentWord,
                                    explanationScores = previousState.explanationScores,
                                    guessingScores = previousState.guessingScores,
                                    wordsStatistic = previousState.wordsStatistic,
                                    wordExplanationStart = previousState.wordExplanationStart,
                                    currentExplanationResults = previousState.currentExplanationResults,
                                )
                            )

                        else -> {
                            transition.stopTimer()

                            if (previousState.settings.strictMode)
                                CheckResult.Success(
                                    GameStateMachine.State.GameInitialised.Round.RoundEditing(
                                        playersList = previousState.playersList,
                                        settings = previousState.settings,
                                        initialWordsNumber = previousState.initialWordsNumber,
                                        roundNumber = previousState.roundNumber,
                                        cycleNumber = previousState.cycleNumber,
                                        speakerIndex = previousState.speakerIndex,
                                        listenerIndex = previousState.listenerIndex,
                                        nextSpeakerIndex = previousState.nextSpeakerIndex,
                                        nextListenerIndex = previousState.nextListenerIndex,
                                        playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                        playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                        restWords = previousState.restWords,
                                        explanationScores = previousState.explanationScores,
                                        guessingScores = previousState.guessingScores,
                                        wordsStatistic = KoneMap.build {
                                            setAllFrom(previousState.wordsStatistic)
                                            setOrChange(
                                                key = previousState.currentWord,
                                                valueOnSet = {
                                                    GameStateMachine.WordStatistic(
                                                        currentInstant - previousState.wordExplanationStart,
                                                        GameStateMachine.WordStatistic.State.InProgress,
                                                    )
                                                },
                                                transformOnChange = {
                                                    GameStateMachine.WordStatistic(
                                                        currentInstant - previousState.wordExplanationStart + it.spentTime,
                                                        it.state,
                                                    )
                                                }
                                            )
                                        },
                                        currentExplanationResults = KoneList.generate(previousState.currentExplanationResults.size + 1u) {
                                            when (it) {
                                                in previousState.currentExplanationResults.indices -> previousState.currentExplanationResults[it]
                                                else -> GameStateMachine.WordExplanation(
                                                    previousState.currentWord,
                                                    GameStateMachine.WordExplanation.State.NotExplained
                                                )
                                            }
                                        },
                                    )
                                )
                            else
                                CheckResult.Success(
                                    GameStateMachine.State.GameInitialised.Round.RoundLastGuess(
                                        playersList = previousState.playersList,
                                        settings = previousState.settings,
                                        initialWordsNumber = previousState.initialWordsNumber,
                                        roundNumber = previousState.roundNumber,
                                        cycleNumber = previousState.cycleNumber,
                                        speakerIndex = previousState.speakerIndex,
                                        listenerIndex = previousState.listenerIndex,
                                        nextSpeakerIndex = previousState.nextSpeakerIndex,
                                        nextListenerIndex = previousState.nextListenerIndex,
                                        playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                        playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                        startInstant = previousState.startInstant,
                                        millisecondsLeft = 0u,
                                        restWords = previousState.restWords,
                                        currentWord = previousState.currentWord,
                                        explanationScores = previousState.explanationScores,
                                        guessingScores = previousState.guessingScores,
                                        wordsStatistic = previousState.wordsStatistic,
                                        wordExplanationStart = previousState.wordExplanationStart,
                                        currentExplanationResults = previousState.currentExplanationResults,
                                    )
                                )
                        }
                    }
                }
                is GameStateMachine.Transition.WordExplanationState -> {
                    val currentInstant = Clock.System.now()
                    CheckResult.Success(
                        GameStateMachine.State.GameInitialised.Round.RoundEditing(
                            playersList = previousState.playersList,
                            settings = previousState.settings,
                            initialWordsNumber = previousState.initialWordsNumber,
                            roundNumber = previousState.roundNumber,
                            cycleNumber = previousState.cycleNumber,
                            speakerIndex = previousState.speakerIndex,
                            listenerIndex = previousState.listenerIndex,
                            nextSpeakerIndex = previousState.nextSpeakerIndex,
                            nextListenerIndex = previousState.nextListenerIndex,
                            playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                            playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                            restWords = previousState.restWords,
                            explanationScores = previousState.explanationScores,
                            guessingScores = previousState.guessingScores,
                            wordsStatistic = KoneMap.build {
                                setAllFrom(previousState.wordsStatistic)
                                setOrChange(
                                    key = previousState.currentWord,
                                    valueOnSet = {
                                        GameStateMachine.WordStatistic(
                                            currentInstant - previousState.wordExplanationStart,
                                            GameStateMachine.WordStatistic.State.InProgress,
                                        )
                                    },
                                    transformOnChange = {
                                        GameStateMachine.WordStatistic(
                                            currentInstant - previousState.wordExplanationStart + it.spentTime,
                                            it.state,
                                        )
                                    }
                                )
                            },
                            currentExplanationResults = KoneList.generate(previousState.currentExplanationResults.size + 1u) {
                                when (it) {
                                    in previousState.currentExplanationResults.indices -> previousState.currentExplanationResults[it]
                                    else -> GameStateMachine.WordExplanation(previousState.currentWord, transition.wordState)
                                }
                            },
                        )
                    )
                }
                is GameStateMachine.Transition.UpdateWordsExplanationResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateWordExplanationResultsNotDuringRoundEditing)
                GameStateMachine.Transition.ConfirmWordsExplanationResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotConfirmWordExplanationResultsNotDuringRoundEditing)
                GameStateMachine.Transition.FinishGame -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotFinishGameNotDuringRoundWaiting)
            }
        is GameStateMachine.State.GameInitialised.Round.RoundEditing ->
            when (transition) {
                is GameStateMachine.Transition.UpdateGameSettings -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateGameSettingsAfterInitialization)
                is GameStateMachine.Transition.InitialiseGame -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotInitializeGameAfterInitialization)
                is GameStateMachine.Transition.SubmitPlayerWords -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitPlayerWordsNotDuringPlayersWordsCollection)
                GameStateMachine.Transition.SpeakerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerReadinessNotDuringRoundWaiting)
                GameStateMachine.Transition.ListenerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetListenerReadinessNotDuringRoundWaiting)
                GameStateMachine.Transition.SpeakerAndListenerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting)
                is GameStateMachine.Transition.UpdateRoundInfo -> {
                    transition.stopTimer()
                    CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                }
                is GameStateMachine.Transition.WordExplanationState -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess)
                is GameStateMachine.Transition.UpdateWordsExplanationResults -> {
                    val oldSortedExplanationResults = previousState.currentExplanationResults.sortedBy { it.word }
                    val newSortedExplanationResults = transition.newExplanationResults.sortedBy { it.word }
                    if (
                        oldSortedExplanationResults.size != newSortedExplanationResults.size ||
                        oldSortedExplanationResults.anyIndexed { index, explanation -> newSortedExplanationResults[index].word != explanation.word }
                    )
                        CheckResult.Failure(
                            GameStateMachine.NoNextStateReason.CannotUpdateWordExplanationResultsWithOtherWordsSet
                        )
                    else
                        CheckResult.Success(
                            GameStateMachine.State.GameInitialised.Round.RoundEditing(
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                initialWordsNumber = previousState.initialWordsNumber,
                                roundNumber = previousState.roundNumber,
                                cycleNumber = previousState.cycleNumber,
                                speakerIndex = previousState.speakerIndex,
                                listenerIndex = previousState.listenerIndex,
                                nextSpeakerIndex = previousState.nextSpeakerIndex,
                                nextListenerIndex = previousState.nextListenerIndex,
                                playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                                playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                                restWords = previousState.restWords,
                                explanationScores = previousState.explanationScores,
                                guessingScores = previousState.guessingScores,
                                wordsStatistic = previousState.wordsStatistic,
                                currentExplanationResults = transition.newExplanationResults,
                            )
                        )
                }
                GameStateMachine.Transition.ConfirmWordsExplanationResults -> {
                    val newRestWords = KoneSet.build<String> {
                        this += previousState.restWords
                        this += previousState.currentExplanationResults.filter { it.state == GameStateMachine.WordExplanation.State.NotExplained }.map { it.word }
                    }
                    val numberOfExplainedWords = previousState.currentExplanationResults.count { it.state == GameStateMachine.WordExplanation.State.Explained }
                    val explanationScores = KoneList.generate(previousState.playersList.size) { previousState.explanationScores[it] + if (it == previousState.speakerIndex) numberOfExplainedWords else 0u }
                    val guessingScores = KoneList.generate(previousState.playersList.size) { previousState.guessingScores[it] + if (it == previousState.listenerIndex) numberOfExplainedWords else 0u }
                    val newWordsStatistic = KoneMap.build {
                        setAllFrom(previousState.wordsStatistic)
                        for ((word, state) in previousState.currentExplanationResults)
                            setOrChange(
                                key = word,
                                valueOnSet = {
                                    // TODO: Think about what to to do with this strange case. Log it? Throw an exception?
                                    GameStateMachine.WordStatistic(
                                        Duration.ZERO,
                                        when (state) {
                                            GameStateMachine.WordExplanation.State.Explained -> GameStateMachine.WordStatistic.State.Explained
                                            GameStateMachine.WordExplanation.State.Mistake -> GameStateMachine.WordStatistic.State.Mistake
                                            GameStateMachine.WordExplanation.State.NotExplained -> GameStateMachine.WordStatistic.State.InProgress
                                        },
                                    )
                                },
                                transformOnChange = {
                                    GameStateMachine.WordStatistic(
                                        it.spentTime,
                                        when (state) {
                                            GameStateMachine.WordExplanation.State.Explained -> GameStateMachine.WordStatistic.State.Explained
                                            GameStateMachine.WordExplanation.State.Mistake -> GameStateMachine.WordStatistic.State.Mistake
                                            GameStateMachine.WordExplanation.State.NotExplained -> GameStateMachine.WordStatistic.State.InProgress
                                        },
                                    )
                                }
                            )
                    }

                    val nextRoundNumber = previousState.roundNumber + 1u
                    var nextCycleNumber = previousState.cycleNumber
                    val nextSpeakerIndex = (previousState.speakerIndex + 1u) % previousState.playersList.size
                    var nextListenerIndex = (previousState.listenerIndex + 1u) % previousState.playersList.size
                    if (nextSpeakerIndex == 0u) {
                        nextListenerIndex = (nextListenerIndex + 1u) % previousState.playersList.size
                        if (nextListenerIndex == 0u) {
                            nextListenerIndex = 1u
                            nextCycleNumber++
                        }
                    }
                    if (
                        newRestWords.isEmpty() || when (val endCondition = previousState.settings.gameEndCondition) {
                            is GameStateMachine.GameEndCondition.Cycles -> endCondition.number == nextCycleNumber
                            is GameStateMachine.GameEndCondition.Words -> false
                        }
                    )
                        CheckResult.Success(
                            GameStateMachine.State.GameInitialised.GameResults(
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                explanationScores = previousState.explanationScores,
                                guessingScores = previousState.guessingScores,
                                wordsStatistic = newWordsStatistic,
                            )
                        )
                    else {
                        val nextNextPair = nextScheduledPairFor(previousState.playersList.size, ScheduledPair(nextSpeakerIndex, nextListenerIndex))
                        val nextSchedule = scheduleFor(previousState.playersList.size, ScheduledPair(nextSpeakerIndex, nextListenerIndex))
                        CheckResult.Success(
                            GameStateMachine.State.GameInitialised.Round.RoundWaiting(
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                initialWordsNumber = previousState.initialWordsNumber,
                                roundNumber = nextRoundNumber,
                                cycleNumber = nextCycleNumber,
                                speakerIndex = nextSpeakerIndex,
                                listenerIndex = nextListenerIndex,
                                nextSpeakerIndex = nextNextPair.speakerIndex,
                                nextListenerIndex = nextNextPair.listenerIndex,
                                playersRoundsBeforeSpeaking = nextSchedule.playersRoundsBeforeSpeaking,
                                playersRoundsBeforeListening = nextSchedule.playersRoundsBeforeListening,
                                restWords = newRestWords,
                                explanationScores = explanationScores,
                                guessingScores = guessingScores,
                                wordsStatistic = newWordsStatistic,
                                speakerReady = false,
                                listenerReady = false,
                            )
                        )
                    }
                }
                GameStateMachine.Transition.FinishGame -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotFinishGameNotDuringRoundWaiting)
            }
        is GameStateMachine.State.GameInitialised.GameResults ->
            when (transition) {
                is GameStateMachine.Transition.UpdateGameSettings -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateGameSettingsAfterInitialization)
                is GameStateMachine.Transition.InitialiseGame -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotInitializeGameAfterInitialization)
                is GameStateMachine.Transition.SubmitPlayerWords -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitPlayerWordsNotDuringPlayersWordsCollection)
                GameStateMachine.Transition.SpeakerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerReadinessNotDuringRoundWaiting)
                GameStateMachine.Transition.ListenerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetListenerReadinessNotDuringRoundWaiting)
                GameStateMachine.Transition.SpeakerAndListenerReady -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting)
                is GameStateMachine.Transition.UpdateRoundInfo -> {
                    transition.stopTimer()
                    CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                }
                is GameStateMachine.Transition.WordExplanationState -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess)
                is GameStateMachine.Transition.UpdateWordsExplanationResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateWordExplanationResultsNotDuringRoundEditing)
                GameStateMachine.Transition.ConfirmWordsExplanationResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotConfirmWordExplanationResultsNotDuringRoundEditing)
                GameStateMachine.Transition.FinishGame -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotFinishGameNotDuringRoundWaiting)
            }
    }