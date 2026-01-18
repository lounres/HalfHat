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
import dev.lounres.kone.collections.list.KoneSettableList
import dev.lounres.kone.collections.list.empty
import dev.lounres.kone.collections.list.generate
import dev.lounres.kone.collections.list.indices
import dev.lounres.kone.collections.list.toKoneSettableList
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
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration


@PublishedApi
internal inline fun <P, WPID, NoWordsProviderReason, MetadataTransition: Any, GSM> GSM.timer(
    coroutineScope: CoroutineScope,
    timerDelayDuration: Duration,
    roundNumber: UInt,
    crossinline moveState: suspend GSM.(GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>) -> Unit,
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

@PublishedApi
internal suspend inline fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition: Any, NoMetadataTransitionReason, GSM> GSM.checkGameStateMachineTransition(
    coroutineScope: CoroutineScope,
    random: Random = DefaultRandom,
    timerDelayDuration: Duration,
    crossinline moveState: suspend GSM.(GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>) -> Unit,
    checkMetadataUpdate: GSM.(previousState: GameStateMachine.State<P, WPID, Metadata>, metadataTransition: MetadataTransition) -> CheckResult<Metadata, NoMetadataTransitionReason>,
    previousState: GameStateMachine.State<P, WPID, Metadata>,
    transition: GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>,
): CheckResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>> {
    val metadataTransition = transition.metadataTransition
    val newMetadata =
        if (metadataTransition == null) previousState.metadata
        else checkMetadataUpdate(previousState, metadataTransition).let { metadataUpdate ->
            when (metadataUpdate) {
                is CheckResult.Failure<NoMetadataTransitionReason> ->
                    return CheckResult.Failure(GameStateMachine.NoNextStateReason.NoMetadataUpdate(metadataUpdate.reason))
                is CheckResult.Success<Metadata> -> metadataUpdate.nextState
            }
        }
    return when(transition) {
        is GameStateMachine.Transition.NoOperation<MetadataTransition> -> scope {
            val nextState = when (previousState) {
                is GameStateMachine.State.GameInitialisation ->
                    GameStateMachine.State.GameInitialisation(
                        metadata = newMetadata,
                        playersList = previousState.playersList,
                        settingsBuilder = previousState.settingsBuilder,
                    )
                is GameStateMachine.State.PlayersWordsCollection ->
                    GameStateMachine.State.PlayersWordsCollection(
                        metadata = newMetadata,
                        playersList = previousState.playersList,
                        settings = previousState.settings,
                        playersWords = previousState.playersWords,
                    )
                is GameStateMachine.State.RoundWaiting ->
                    GameStateMachine.State.RoundWaiting(
                        metadata = newMetadata,
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
                        speakerReady = previousState.speakerReady,
                        listenerReady = previousState.listenerReady,
                    )
                is GameStateMachine.State.RoundPreparation ->
                    GameStateMachine.State.RoundPreparation(
                        metadata = newMetadata,
                        playersList = previousState.playersList,
                        settings = previousState.settings,
                        roundNumber = previousState.roundNumber,
                        cycleNumber = previousState.cycleNumber,
                        initialWordsNumber = previousState.initialWordsNumber,
                        speakerIndex = previousState.speakerIndex,
                        listenerIndex = previousState.listenerIndex,
                        nextSpeakerIndex = previousState.nextSpeakerIndex,
                        nextListenerIndex = previousState.nextListenerIndex,
                        playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                        playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                        startInstant = previousState.startInstant,
                        millisecondsLeft = previousState.millisecondsLeft,
                        restWords = previousState.restWords,
                        explanationScores = previousState.explanationScores,
                        guessingScores = previousState.guessingScores,
                        currentExplanationResults = previousState.currentExplanationResults,
                    )
                is GameStateMachine.State.RoundExplanation ->
                    GameStateMachine.State.RoundExplanation(
                        metadata = newMetadata,
                        playersList = previousState.playersList,
                        settings = previousState.settings,
                        roundNumber = previousState.roundNumber,
                        cycleNumber = previousState.cycleNumber,
                        initialWordsNumber = previousState.initialWordsNumber,
                        speakerIndex = previousState.speakerIndex,
                        listenerIndex = previousState.listenerIndex,
                        nextSpeakerIndex = previousState.nextSpeakerIndex,
                        nextListenerIndex = previousState.nextListenerIndex,
                        playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                        playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                        startInstant = previousState.startInstant,
                        millisecondsLeft = previousState.millisecondsLeft,
                        restWords = previousState.restWords,
                        currentWord = previousState.currentWord,
                        explanationScores = previousState.explanationScores,
                        guessingScores = previousState.guessingScores,
                        currentExplanationResults = previousState.currentExplanationResults,
                    )
                is GameStateMachine.State.RoundLastGuess ->
                    GameStateMachine.State.RoundLastGuess(
                        metadata = newMetadata,
                        playersList = previousState.playersList,
                        settings = previousState.settings,
                        roundNumber = previousState.roundNumber,
                        cycleNumber = previousState.cycleNumber,
                        initialWordsNumber = previousState.initialWordsNumber,
                        speakerIndex = previousState.speakerIndex,
                        listenerIndex = previousState.listenerIndex,
                        nextSpeakerIndex = previousState.nextSpeakerIndex,
                        nextListenerIndex = previousState.nextListenerIndex,
                        playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                        playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                        startInstant = previousState.startInstant,
                        millisecondsLeft = previousState.millisecondsLeft,
                        restWords = previousState.restWords,
                        currentWord = previousState.currentWord,
                        explanationScores = previousState.explanationScores,
                        guessingScores = previousState.guessingScores,
                        currentExplanationResults = previousState.currentExplanationResults,
                    )
                is GameStateMachine.State.RoundEditing ->
                    GameStateMachine.State.RoundEditing(
                        metadata = newMetadata,
                        playersList = previousState.playersList,
                        settings = previousState.settings,
                        roundNumber = previousState.roundNumber,
                        cycleNumber = previousState.cycleNumber,
                        initialWordsNumber = previousState.initialWordsNumber,
                        speakerIndex = previousState.speakerIndex,
                        listenerIndex = previousState.listenerIndex,
                        nextSpeakerIndex = previousState.nextSpeakerIndex,
                        nextListenerIndex = previousState.nextListenerIndex,
                        playersRoundsBeforeSpeaking = previousState.playersRoundsBeforeSpeaking,
                        playersRoundsBeforeListening = previousState.playersRoundsBeforeListening,
                        restWords = previousState.restWords,
                        explanationScores = previousState.explanationScores,
                        guessingScores = previousState.guessingScores,
                        currentExplanationResults = previousState.currentExplanationResults,
                    )
                is GameStateMachine.State.GameResults ->
                    GameStateMachine.State.GameResults(
                        metadata = newMetadata,
                        playersList = previousState.playersList,
                        settings = previousState.settings,
                        results = previousState.results,
                    )
            }
            CheckResult.Success(nextState)
        }
        is GameStateMachine.Transition.UpdateGameSettings<P, WPID, MetadataTransition> ->
            when (previousState) {
                is GameStateMachine.State.GameInitialisation ->
                    CheckResult.Success(
                        GameStateMachine.State.GameInitialisation(
                            metadata = newMetadata,
                            playersList = transition.playersList,
                            settingsBuilder = transition.settingsBuilder
                        )
                    )
                
                is GameStateMachine.State.PlayersWordsCollection,
                is GameStateMachine.State.RoundWaiting,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateGameSettingsAfterInitialization)
            }
        is GameStateMachine.Transition.InitialiseGame ->
            when (previousState) {
                is GameStateMachine.State.GameInitialisation -> scope {
                    val playersList = previousState.playersList
                    val settingsBuilder = previousState.settingsBuilder
                    
                    if (playersList.size < 2u) return@scope CheckResult.Failure(GameStateMachine.NoNextStateReason.NotEnoughPlayersForInitialization)
                    
                    when (val wordsSource = settingsBuilder.wordsSource) {
                        GameStateMachine.WordsSource.Players ->
                            CheckResult.Success(
                                GameStateMachine.State.PlayersWordsCollection(
                                    metadata = newMetadata,
                                    playersList = playersList,
                                    settings = settingsBuilder.build(),
                                    playersWords = KoneList.generate(playersList.size) { null }
                                )
                            )
                        is GameStateMachine.WordsSource.Custom -> {
                            val wordsProviderOrReason = transition.wordsProviderRegistry[wordsSource.providerId]
                            
                            when (wordsProviderOrReason) {
                                is GameStateMachine.WordsProviderRegistry.ResultOrReason.Failure ->
                                    CheckResult.Failure(GameStateMachine.NoNextStateReason.NoWordsProvider(wordsProviderOrReason.reason))
                                is GameStateMachine.WordsProviderRegistry.ResultOrReason.Success -> {
                                    val restWords = when (settingsBuilder.gameEndConditionType) {
                                        GameStateMachine.GameEndCondition.Type.Words -> {
                                            val provider = wordsProviderOrReason.result
                                            provider.randomWords(min(provider.size, settingsBuilder.cachedEndConditionWordsNumber))
                                        }
                                        GameStateMachine.GameEndCondition.Type.Cycles -> wordsProviderOrReason.result.allWords()
                                    }
                                    val nextPair = nextScheduledPairFor(playersList.size, ScheduledPair(0u, 1u))
                                    val schedule = scheduleFor(playersList.size, ScheduledPair(0u, 1u))
                                    CheckResult.Success(
                                        GameStateMachine.State.RoundWaiting(
                                            metadata = newMetadata,
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
                                            speakerReady = false,
                                            listenerReady = false,
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                
                is GameStateMachine.State.PlayersWordsCollection,
                is GameStateMachine.State.RoundWaiting,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotInitializeGameAfterInitialization)
            }
        is GameStateMachine.Transition.SubmitPlayerWords ->
            when (previousState) {
                is GameStateMachine.State.PlayersWordsCollection ->
                    if (previousState.playersList[transition.playerIndex] != null)
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
                                GameStateMachine.State.RoundWaiting(
                                    metadata = newMetadata,
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
                                    speakerReady = false,
                                    listenerReady = false,
                                )
                            )
                        } else
                            CheckResult.Success(
                                GameStateMachine.State.PlayersWordsCollection(
                                    metadata = newMetadata,
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
                                    playersWords = newPlayersWords,
                                )
                            )
                    }
                    
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.RoundWaiting,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitPlayerWordsNotDuringPlayersWordsCollection)
            }
        is GameStateMachine.Transition.SpeakerReady ->
            when (previousState) {
                is GameStateMachine.State.RoundWaiting ->
                    if (previousState.listenerReady) {
                        timer(
                            coroutineScope = coroutineScope,
                            timerDelayDuration = timerDelayDuration,
                            roundNumber = previousState.roundNumber,
                            moveState = moveState,
                        )
                        CheckResult.Success(
                            GameStateMachine.State.RoundPreparation(
                                metadata = newMetadata,
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
                                currentExplanationResults = KoneList.empty(),
                            )
                        )
                    }
                    else
                        CheckResult.Success(
                            GameStateMachine.State.RoundWaiting(
                                metadata = newMetadata,
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
                                speakerReady = true,
                                listenerReady = previousState.listenerReady,
                            )
                        )
                
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.PlayersWordsCollection,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerReadinessNotDuringRoundWaiting)
            }
        is GameStateMachine.Transition.ListenerReady ->
            when (previousState) {
                is GameStateMachine.State.RoundWaiting ->
                    if (previousState.speakerReady) {
                        timer(
                            coroutineScope = coroutineScope,
                            timerDelayDuration = timerDelayDuration,
                            roundNumber = previousState.roundNumber,
                            moveState = moveState,
                        )
                        CheckResult.Success(
                            GameStateMachine.State.RoundPreparation(
                                metadata = newMetadata,
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
                                currentExplanationResults = KoneList.empty(),
                            )
                        )
                    }
                    else
                        CheckResult.Success(
                            GameStateMachine.State.RoundWaiting(
                                metadata = newMetadata,
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
                                speakerReady = previousState.speakerReady,
                                listenerReady = true,
                            )
                        )
                
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.PlayersWordsCollection,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetListenerReadinessNotDuringRoundWaiting)
            }
        is GameStateMachine.Transition.SpeakerAndListenerReady ->
            when (previousState) {
                is GameStateMachine.State.RoundWaiting -> {
                    timer(
                        coroutineScope = coroutineScope,
                        timerDelayDuration = timerDelayDuration,
                        roundNumber = previousState.roundNumber,
                        moveState = moveState,
                    )
                    CheckResult.Success(
                        GameStateMachine.State.RoundPreparation(
                            metadata = newMetadata,
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
                            currentExplanationResults = KoneList.empty(),
                        )
                    )
                }
                
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.PlayersWordsCollection,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting)
            }
        is GameStateMachine.Transition.UpdateRoundInfo ->
            when (previousState) {
                is GameStateMachine.State.GameInitialisation -> {
                    transition.stopTimer()
                    CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                }
                is GameStateMachine.State.PlayersWordsCollection -> {
                    transition.stopTimer()
                    CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                }
                is GameStateMachine.State.RoundWaiting -> {
                    transition.stopTimer()
                    CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                }
                is GameStateMachine.State.RoundPreparation -> {
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
                                GameStateMachine.State.RoundPreparation(
                                    metadata = newMetadata,
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
                                    currentExplanationResults = previousState.currentExplanationResults,
                                )
                            )
                        
                        spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds) * 1000u -> {
                            val currentWord = previousState.restWords.random(random)
                            val restWords =
                                previousState.restWords.filterTo(KoneMutableSet.of()) { it != currentWord }
                            CheckResult.Success(
                                GameStateMachine.State.RoundExplanation(
                                    metadata = newMetadata,
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
                                    currentExplanationResults = previousState.currentExplanationResults,
                                )
                            )
                        }
                        
                        spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u -> {
                            val currentWord = previousState.restWords.random(random)
                            val restWords =
                                previousState.restWords.filterTo(KoneMutableSet.of()) { it != currentWord }
                            CheckResult.Success(
                                GameStateMachine.State.RoundLastGuess(
                                    metadata = newMetadata,
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
                                    currentExplanationResults = previousState.currentExplanationResults,
                                )
                            )
                        }
                        
                        else -> {
                            transition.stopTimer()
                            
                            if (previousState.settings.strictMode)
                                CheckResult.Success(
                                    GameStateMachine.State.RoundEditing(
                                        metadata = newMetadata,
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
                                        currentExplanationResults = previousState.currentExplanationResults,
                                    )
                                )
                            else {
                                val currentWord = previousState.restWords.random(random)
                                val restWords =
                                    previousState.restWords.filterTo(KoneMutableSet.of()) { it != currentWord }
                                CheckResult.Success(
                                    GameStateMachine.State.RoundLastGuess(
                                        metadata = newMetadata,
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
                                        currentExplanationResults = previousState.currentExplanationResults,
                                    )
                                )
                            }
                        }
                    }
                }
                is GameStateMachine.State.RoundExplanation -> {
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
                                GameStateMachine.State.RoundExplanation(
                                    metadata = newMetadata,
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
                                    currentExplanationResults = previousState.currentExplanationResults,
                                )
                            )
                        
                        spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u ->
                            CheckResult.Success(
                                GameStateMachine.State.RoundLastGuess(
                                    metadata = newMetadata,
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
                                    currentExplanationResults = previousState.currentExplanationResults,
                                )
                            )
                        
                        else -> {
                            transition.stopTimer()
                            
                            if (previousState.settings.strictMode)
                                CheckResult.Success(
                                    GameStateMachine.State.RoundEditing(
                                        metadata = newMetadata,
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
                                    GameStateMachine.State.RoundLastGuess(
                                        metadata = newMetadata,
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
                                        currentExplanationResults = previousState.currentExplanationResults,
                                    )
                                )
                        }
                    }
                }
                is GameStateMachine.State.RoundLastGuess -> {
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
                                GameStateMachine.State.RoundLastGuess(
                                    metadata = newMetadata,
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
                                    currentExplanationResults = previousState.currentExplanationResults,
                                )
                            )
                        
                        else -> {
                            transition.stopTimer()
                            
                            if (previousState.settings.strictMode)
                                CheckResult.Success(
                                    GameStateMachine.State.RoundEditing(
                                        metadata = newMetadata,
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
                                    GameStateMachine.State.RoundLastGuess(
                                        metadata = newMetadata,
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
                                        currentExplanationResults = previousState.currentExplanationResults,
                                    )
                                )
                        }
                    }
                }
                is GameStateMachine.State.RoundEditing -> {
                    transition.stopTimer()
                    CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                }
                is GameStateMachine.State.GameResults -> {
                    transition.stopTimer()
                    CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                }
            }
        is GameStateMachine.Transition.WordExplanationState ->
            when (previousState) {
                is GameStateMachine.State.RoundExplanation ->
                    if (previousState.restWords.isNotEmpty() && transition.wordState == GameStateMachine.WordExplanation.State.Explained) {
                        val nextCurrentWord = previousState.restWords.random(Random)
                        val nextRestWords = previousState.restWords.filterTo(KoneMutableSet.of()) { it != nextCurrentWord }
                        CheckResult.Success(
                            GameStateMachine.State.RoundExplanation(
                                metadata = newMetadata,
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
                                currentExplanationResults = KoneList.generate(previousState.currentExplanationResults.size + 1u) {
                                    when (it) {
                                        in previousState.currentExplanationResults.indices -> previousState.currentExplanationResults[it]
                                        else -> GameStateMachine.WordExplanation(previousState.currentWord, transition.wordState)
                                    }
                                },
                            )
                        )
                    } else {
                        CheckResult.Success(
                            GameStateMachine.State.RoundEditing(
                                metadata = newMetadata,
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
                is GameStateMachine.State.RoundLastGuess -> {
                    CheckResult.Success(
                        GameStateMachine.State.RoundEditing(
                            metadata = newMetadata,
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
                            currentExplanationResults = KoneList.generate(previousState.currentExplanationResults.size + 1u) {
                                when (it) {
                                    in previousState.currentExplanationResults.indices -> previousState.currentExplanationResults[it]
                                    else -> GameStateMachine.WordExplanation(previousState.currentWord, transition.wordState)
                                }
                            },
                        )
                    )
                }
                
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.PlayersWordsCollection,
                is GameStateMachine.State.RoundWaiting,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess)
            }
        is GameStateMachine.Transition.UpdateWordsExplanationResults ->
            when (previousState) {
                is GameStateMachine.State.RoundEditing ->
                    CheckResult.Success(
                        GameStateMachine.State.RoundEditing(
                            metadata = newMetadata,
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
                            currentExplanationResults = transition.newExplanationResults,
                        )
                    )
                
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.PlayersWordsCollection,
                is GameStateMachine.State.RoundWaiting,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateWordExplanationResultsNotDuringRoundEditing)
            }
        is GameStateMachine.Transition.ConfirmWordsExplanationResults ->
            when (previousState) {
                is GameStateMachine.State.RoundEditing -> {
                    val newRestWords = KoneSet.build<String> {
                        this += previousState.restWords
                        this += previousState.currentExplanationResults.filter { it.state == GameStateMachine.WordExplanation.State.NotExplained }.map { it.word }
                    }
                    val numberOfExplainedWords = previousState.currentExplanationResults.count { it.state == GameStateMachine.WordExplanation.State.Explained }
                    val explanationScores = KoneList.generate(previousState.playersList.size) { previousState.explanationScores[it] + if (it == previousState.speakerIndex) numberOfExplainedWords else 0u }
                    val guessingScores = KoneList.generate(previousState.playersList.size) { previousState.guessingScores[it] + if (it == previousState.listenerIndex) numberOfExplainedWords else 0u }
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
                            GameStateMachine.State.GameResults(
                                metadata = newMetadata,
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                results = KoneSettableList.generate(previousState.playersList.size) {
                                    GameStateMachine.GameResult(
                                        it,
                                        explanationScores[it],
                                        guessingScores[it],
                                        explanationScores[it] + guessingScores[it],
                                    )
                                }.apply { sortByDescending { it.scoreSum } }
                            )
                        )
                    else {
                        val nextNextPair = nextScheduledPairFor(previousState.playersList.size, ScheduledPair(nextSpeakerIndex, nextListenerIndex))
                        val nextSchedul = scheduleFor(previousState.playersList.size, ScheduledPair(nextSpeakerIndex, nextListenerIndex))
                        CheckResult.Success(
                            GameStateMachine.State.RoundWaiting(
                                metadata = newMetadata,
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                initialWordsNumber = previousState.initialWordsNumber,
                                roundNumber = nextRoundNumber,
                                cycleNumber = nextCycleNumber,
                                speakerIndex = nextSpeakerIndex,
                                listenerIndex = nextListenerIndex,
                                nextSpeakerIndex = nextNextPair.speakerIndex,
                                nextListenerIndex = nextNextPair.listenerIndex,
                                playersRoundsBeforeSpeaking = nextSchedul.playersRoundsBeforeSpeaking,
                                playersRoundsBeforeListening = nextSchedul.playersRoundsBeforeListening,
                                restWords = newRestWords,
                                explanationScores = explanationScores,
                                guessingScores = guessingScores,
                                speakerReady = false,
                                listenerReady = false,
                            )
                        )
                    }
                }
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.PlayersWordsCollection,
                is GameStateMachine.State.RoundWaiting,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotConfirmWordExplanationResultsNotDuringRoundEditing)
            }
        is GameStateMachine.Transition.FinishGame ->
            when (previousState) {
                is GameStateMachine.State.RoundWaiting ->
                    CheckResult.Success(
                        GameStateMachine.State.GameResults(
                            metadata = newMetadata,
                            playersList = previousState.playersList,
                            settings = previousState.settings,
                            results = KoneSettableList.generate(previousState.playersList.size) {
                                GameStateMachine.GameResult(
                                    player = it,
                                    scoreExplained = previousState.explanationScores[it],
                                    scoreGuessed = previousState.guessingScores[it],
                                    scoreSum = previousState.explanationScores[it] + previousState.guessingScores[it],
                                )
                            }.apply { sortByDescending { it.scoreSum } }
                        )
                    )
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.PlayersWordsCollection,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotFinishGameNotDuringRoundWaiting)
            }
    }
}