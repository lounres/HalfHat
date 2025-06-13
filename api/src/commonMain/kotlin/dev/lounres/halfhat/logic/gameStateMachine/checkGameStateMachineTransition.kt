package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.automata.CheckResult
import dev.lounres.kone.collections.iterables.isEmpty
import dev.lounres.kone.collections.iterables.isNotEmpty
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.KoneSettableList
import dev.lounres.kone.collections.list.empty
import dev.lounres.kone.collections.list.indices
import dev.lounres.kone.collections.set.KoneMutableSet
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.build
import dev.lounres.kone.collections.set.of
import dev.lounres.kone.collections.utils.count
import dev.lounres.kone.collections.utils.filter
import dev.lounres.kone.collections.utils.filterTo
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.collections.utils.plusAssign
import dev.lounres.kone.collections.utils.random
import dev.lounres.kone.collections.utils.sortByDescending
import dev.lounres.kone.scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.math.min
import kotlin.random.Random


@PublishedApi
internal inline fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason, GSM> GSM.checkGameStateMachineTransition(
    coroutineScope: CoroutineScope,
    random: Random = DefaultRandom,
    crossinline moveState: suspend GSM.(GameStateMachine.Transition<P, WP, MetadataTransition>) -> Unit,
    checkMetadataUpdate: GSM.(previousState: GameStateMachine.State<P, WP, Metadata>, metadataTransition: MetadataTransition) -> CheckResult<Metadata, NoMetadataTransitionReason>,
    metadataTransformer: GSM.(previousState: GameStateMachine.State<P, WP, Metadata>, transition: GameStateMachine.Transition.UpdateGame<P, WP>) -> Metadata = { previousState, _ -> previousState.metadata },
    previousState: GameStateMachine.State<P, WP, Metadata>,
    transition: GameStateMachine.Transition<P, WP, MetadataTransition>,
): CheckResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    when(transition) {
        is GameStateMachine.Transition.UpdateMetadata<MetadataTransition> -> scope {
            val newMetadata = checkMetadataUpdate(previousState, transition.metadataTransition).let {
                when (it) {
                    is CheckResult.Failure<NoMetadataTransitionReason> -> TODO()
                    is CheckResult.Success<Metadata> -> it.nextState
                }
            }
            val nextState = when (previousState) {
                is GameStateMachine.State.GameInitialisation ->
                    GameStateMachine.State.GameInitialisation(
                        metadata = newMetadata,
                        playersList = previousState.playersList,
                        settingsBuilder = previousState.settingsBuilder,
                    )
                is GameStateMachine.State.RoundWaiting ->
                    GameStateMachine.State.RoundWaiting(
                        metadata = newMetadata,
                        playersList = previousState.playersList,
                        settings = previousState.settings,
                        roundNumber = previousState.roundNumber,
                        cycleNumber = previousState.cycleNumber,
                        speakerIndex = previousState.speakerIndex,
                        listenerIndex = previousState.listenerIndex,
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
                        speakerIndex = previousState.speakerIndex,
                        listenerIndex = previousState.listenerIndex,
                        startInstant = previousState.startInstant,
                        millisecondsLeft = previousState.millisecondsLeft,
                        restWords = previousState.restWords,
                        explanationScores = previousState.explanationScores,
                        guessingScores = previousState.guessingScores,
                        currentExplanationResults = previousState.currentExplanationResults,
                        timer = previousState.timer,
                    )
                is GameStateMachine.State.RoundExplanation ->
                    GameStateMachine.State.RoundExplanation(
                        metadata = newMetadata,
                        playersList = previousState.playersList,
                        settings = previousState.settings,
                        roundNumber = previousState.roundNumber,
                        cycleNumber = previousState.cycleNumber,
                        speakerIndex = previousState.speakerIndex,
                        listenerIndex = previousState.listenerIndex,
                        startInstant = previousState.startInstant,
                        millisecondsLeft = previousState.millisecondsLeft,
                        restWords = previousState.restWords,
                        currentWord = previousState.currentWord,
                        explanationScores = previousState.explanationScores,
                        guessingScores = previousState.guessingScores,
                        currentExplanationResults = previousState.currentExplanationResults,
                        timer = previousState.timer,
                    )
                is GameStateMachine.State.RoundLastGuess ->
                    GameStateMachine.State.RoundLastGuess(
                        metadata = newMetadata,
                        playersList = previousState.playersList,
                        settings = previousState.settings,
                        roundNumber = previousState.roundNumber,
                        cycleNumber = previousState.cycleNumber,
                        speakerIndex = previousState.speakerIndex,
                        listenerIndex = previousState.listenerIndex,
                        startInstant = previousState.startInstant,
                        millisecondsLeft = previousState.millisecondsLeft,
                        restWords = previousState.restWords,
                        currentWord = previousState.currentWord,
                        explanationScores = previousState.explanationScores,
                        guessingScores = previousState.guessingScores,
                        currentExplanationResults = previousState.currentExplanationResults,
                        timer = previousState.timer,
                    )
                is GameStateMachine.State.RoundEditing ->
                    GameStateMachine.State.RoundEditing(
                        metadata = newMetadata,
                        playersList = previousState.playersList,
                        settings = previousState.settings,
                        roundNumber = previousState.roundNumber,
                        cycleNumber = previousState.cycleNumber,
                        speakerIndex = previousState.speakerIndex,
                        listenerIndex = previousState.listenerIndex,
                        restWords = previousState.restWords,
                        explanationScores = previousState.explanationScores,
                        guessingScores = previousState.guessingScores,
                        currentExplanationResults = previousState.currentExplanationResults,
                    )
                is GameStateMachine.State.GameResults ->
                    GameStateMachine.State.GameResults(
                        metadata = newMetadata,
                        playersList = previousState.playersList,
                        results = previousState.results,
                    )
            }
            CheckResult.Success(nextState)
        }
        is GameStateMachine.Transition.UpdateGame.UpdateGameSettings<P, WP> ->
            when (previousState) {
                is GameStateMachine.State.GameInitialisation ->
                    CheckResult.Success(
                        GameStateMachine.State.GameInitialisation(
                            metadata = metadataTransformer(previousState, transition),
                            playersList = transition.playersList,
                            settingsBuilder = transition.settingsBuilder
                        )
                    )
                
                is GameStateMachine.State.RoundWaiting,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateGameSettingsAfterInitialization)
            }
        is GameStateMachine.Transition.UpdateGame.InitialiseGame ->
            when (previousState) {
                is GameStateMachine.State.GameInitialisation -> scope {
                    val playersList = previousState.playersList
                    val settingsBuilder = previousState.settingsBuilder
                    
                    if (playersList.size < 2u) return@scope CheckResult.Failure(GameStateMachine.NoNextStateReason.NotEnoughPlayersForInitialization)
                    
                    CheckResult.Success(
                        GameStateMachine.State.RoundWaiting(
                            metadata = metadataTransformer(previousState, transition),
                            playersList = playersList,
                            settings = settingsBuilder.build(),
                            roundNumber = 0u,
                            cycleNumber = 0u,
                            speakerIndex = 0u,
                            listenerIndex = 1u,
                            restWords = when (val wordsSource = settingsBuilder.wordsSource) {
                                GameStateMachine.WordsSource.Players -> TODO()
                                is GameStateMachine.WordsSource.Custom<*> ->
                                    when (settingsBuilder.gameEndConditionType) {
                                        GameStateMachine.GameEndCondition.Type.Words ->
                                            wordsSource.provider.randomWords(min(wordsSource.provider.size, settingsBuilder.cachedEndConditionWordsNumber))
                                        GameStateMachine.GameEndCondition.Type.Cycles -> wordsSource.provider.allWords()
                                    }
                            },
                            explanationScores = KoneList(playersList.size) { 0u },
                            guessingScores = KoneList(playersList.size) { 0u },
                            speakerReady = false,
                            listenerReady = false,
                        )
                    )
                }
                
                is GameStateMachine.State.RoundWaiting,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotInitializationGameSettingsAfterInitialization)
            }
        is GameStateMachine.Transition.UpdateGame.SpeakerReady ->
            when (previousState) {
                is GameStateMachine.State.RoundWaiting ->
                    if (previousState.listenerReady)
                        CheckResult.Success(
                            GameStateMachine.State.RoundPreparation(
                                metadata = metadataTransformer(previousState, transition),
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                roundNumber = previousState.roundNumber,
                                cycleNumber = previousState.cycleNumber,
                                speakerIndex = previousState.speakerIndex,
                                listenerIndex = previousState.listenerIndex,
                                startInstant = Clock.System.now(),
                                millisecondsLeft = previousState.settings.preparationTimeSeconds * 1000u,
                                restWords = previousState.restWords,
                                explanationScores = previousState.explanationScores,
                                guessingScores = previousState.guessingScores,
                                currentExplanationResults = KoneList.empty(),
                                timer = coroutineScope.launch {
                                    while (true) {
                                        moveState(GameStateMachine.Transition.UpdateGame.UpdateRoundInfo(roundNumber = previousState.roundNumber))
                                    }
                                }
                            )
                        )
                    else
                        CheckResult.Success(
                            GameStateMachine.State.RoundWaiting(
                                metadata = metadataTransformer(previousState, transition),
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                roundNumber = previousState.roundNumber,
                                cycleNumber = previousState.cycleNumber,
                                speakerIndex = previousState.speakerIndex,
                                listenerIndex = previousState.listenerIndex,
                                restWords = previousState.restWords,
                                explanationScores = previousState.explanationScores,
                                guessingScores = previousState.guessingScores,
                                speakerReady = true,
                                listenerReady = previousState.listenerReady,
                            )
                        )
                
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerReadinessNotDuringRoundWaiting)
            }
        is GameStateMachine.Transition.UpdateGame.ListenerReady ->
            when (previousState) {
                is GameStateMachine.State.RoundWaiting ->
                    if (previousState.speakerReady)
                        CheckResult.Success(
                            GameStateMachine.State.RoundPreparation(
                                metadata = metadataTransformer(previousState, transition),
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                roundNumber = previousState.roundNumber,
                                cycleNumber = previousState.cycleNumber,
                                speakerIndex = previousState.speakerIndex,
                                listenerIndex = previousState.listenerIndex,
                                startInstant = Clock.System.now(),
                                millisecondsLeft = previousState.settings.preparationTimeSeconds * 1000u,
                                restWords = previousState.restWords,
                                explanationScores = previousState.explanationScores,
                                guessingScores = previousState.guessingScores,
                                currentExplanationResults = KoneList.empty(),
                                timer = coroutineScope.launch {
                                    while (true) {
                                        moveState(GameStateMachine.Transition.UpdateGame.UpdateRoundInfo(roundNumber = previousState.roundNumber))
                                    }
                                }
                            )
                        )
                    else
                        CheckResult.Success(
                            GameStateMachine.State.RoundWaiting(
                                metadata = metadataTransformer(previousState, transition),
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                roundNumber = previousState.roundNumber,
                                cycleNumber = previousState.cycleNumber,
                                speakerIndex = previousState.speakerIndex,
                                listenerIndex = previousState.listenerIndex,
                                restWords = previousState.restWords,
                                explanationScores = previousState.explanationScores,
                                guessingScores = previousState.guessingScores,
                                speakerReady = previousState.speakerReady,
                                listenerReady = true,
                            )
                        )
                
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetListenerReadinessNotDuringRoundWaiting)
            }
        is GameStateMachine.Transition.UpdateGame.SpeakerAndListenerReady ->
            when (previousState) {
                is GameStateMachine.State.RoundWaiting ->
                    CheckResult.Success(
                        GameStateMachine.State.RoundPreparation(
                            metadata = metadataTransformer(previousState, transition),
                            playersList = previousState.playersList,
                            settings = previousState.settings,
                            roundNumber = previousState.roundNumber,
                            cycleNumber = previousState.cycleNumber,
                            speakerIndex = previousState.speakerIndex,
                            listenerIndex = previousState.listenerIndex,
                            startInstant = Clock.System.now(),
                            millisecondsLeft = previousState.settings.preparationTimeSeconds * 1000u,
                            restWords = previousState.restWords,
                            explanationScores = previousState.explanationScores,
                            guessingScores = previousState.guessingScores,
                            currentExplanationResults = KoneList.empty(),
                            timer = coroutineScope.launch {
                                while (true) {
                                    moveState(GameStateMachine.Transition.UpdateGame.UpdateRoundInfo(roundNumber = previousState.roundNumber))
                                }
                            }
                        )
                    )
                
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting)
            }
        is GameStateMachine.Transition.UpdateGame.UpdateRoundInfo ->
            when (previousState) {
                is GameStateMachine.State.GameInitialisation -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                is GameStateMachine.State.RoundWaiting -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                is GameStateMachine.State.RoundPreparation -> {
                    val currentInstant = Clock.System.now()
                    val spentTimeMilliseconds = (currentInstant - previousState.startInstant).inWholeMilliseconds.toUInt()
                    val preparationTimeSeconds = previousState.settings.preparationTimeSeconds
                    val explanationTimeSeconds = previousState.settings.explanationTimeSeconds
                    val finalGuessTimeSeconds = previousState.settings.finalGuessTimeSeconds
                    when {
                        previousState.roundNumber != transition.roundNumber -> {
                            previousState.timer.cancel()
                            CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                        }
                        spentTimeMilliseconds < preparationTimeSeconds * 1000u ->
                            CheckResult.Success(
                                GameStateMachine.State.RoundPreparation(
                                    metadata = metadataTransformer(previousState, transition),
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
                                    roundNumber = previousState.roundNumber,
                                    cycleNumber = previousState.cycleNumber,
                                    speakerIndex = previousState.speakerIndex,
                                    listenerIndex = previousState.listenerIndex,
                                    startInstant = previousState.startInstant,
                                    millisecondsLeft = preparationTimeSeconds * 1000u - spentTimeMilliseconds,
                                    restWords = previousState.restWords,
                                    explanationScores = previousState.explanationScores,
                                    guessingScores = previousState.guessingScores,
                                    currentExplanationResults = previousState.currentExplanationResults,
                                    timer = previousState.timer,
                                )
                            )
                        
                        spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds) * 1000u -> {
                            val currentWord = previousState.restWords.random(random)
                            val restWords =
                                previousState.restWords.filterTo(KoneMutableSet.of()) { it != currentWord }
                            CheckResult.Success(
                                GameStateMachine.State.RoundExplanation(
                                    metadata = metadataTransformer(previousState, transition),
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
                                    roundNumber = previousState.roundNumber,
                                    cycleNumber = previousState.cycleNumber,
                                    speakerIndex = previousState.speakerIndex,
                                    listenerIndex = previousState.listenerIndex,
                                    startInstant = previousState.startInstant,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = restWords,
                                    currentWord = currentWord,
                                    explanationScores = previousState.explanationScores,
                                    guessingScores = previousState.guessingScores,
                                    currentExplanationResults = previousState.currentExplanationResults,
                                    timer = previousState.timer,
                                )
                            )
                        }
                        
                        spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u -> {
                            val currentWord = previousState.restWords.random(random)
                            val restWords =
                                previousState.restWords.filterTo(KoneMutableSet.of()) { it != currentWord }
                            CheckResult.Success(
                                GameStateMachine.State.RoundLastGuess(
                                    metadata = metadataTransformer(previousState, transition),
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
                                    roundNumber = previousState.roundNumber,
                                    cycleNumber = previousState.cycleNumber,
                                    speakerIndex = previousState.speakerIndex,
                                    listenerIndex = previousState.listenerIndex,
                                    startInstant = previousState.startInstant,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = restWords,
                                    currentWord = currentWord,
                                    explanationScores = previousState.explanationScores,
                                    guessingScores = previousState.guessingScores,
                                    currentExplanationResults = previousState.currentExplanationResults,
                                    timer = previousState.timer,
                                )
                            )
                        }
                        
                        else -> {
                            previousState.timer.cancel()
                            if (previousState.settings.strictMode)
                                CheckResult.Success(
                                    GameStateMachine.State.RoundEditing(
                                        metadata = metadataTransformer(previousState, transition),
                                        playersList = previousState.playersList,
                                        settings = previousState.settings,
                                        roundNumber = previousState.roundNumber,
                                        cycleNumber = previousState.cycleNumber,
                                        speakerIndex = previousState.speakerIndex,
                                        listenerIndex = previousState.listenerIndex,
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
                                        metadata = metadataTransformer(previousState, transition),
                                        playersList = previousState.playersList,
                                        settings = previousState.settings,
                                        roundNumber = previousState.roundNumber,
                                        cycleNumber = previousState.cycleNumber,
                                        speakerIndex = previousState.speakerIndex,
                                        listenerIndex = previousState.listenerIndex,
                                        startInstant = previousState.startInstant,
                                        millisecondsLeft = 0u,
                                        restWords = restWords,
                                        currentWord = currentWord,
                                        explanationScores = previousState.explanationScores,
                                        guessingScores = previousState.guessingScores,
                                        currentExplanationResults = previousState.currentExplanationResults,
                                        timer = previousState.timer,
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
                            previousState.timer.cancel()
                            CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                        }
                        spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds) * 1000u ->
                            CheckResult.Success(
                                GameStateMachine.State.RoundExplanation(
                                    metadata = metadataTransformer(previousState, transition),
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
                                    roundNumber = previousState.roundNumber,
                                    cycleNumber = previousState.cycleNumber,
                                    speakerIndex = previousState.speakerIndex,
                                    listenerIndex = previousState.listenerIndex,
                                    startInstant = previousState.startInstant,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = previousState.restWords,
                                    currentWord = previousState.currentWord,
                                    explanationScores = previousState.explanationScores,
                                    guessingScores = previousState.guessingScores,
                                    currentExplanationResults = previousState.currentExplanationResults,
                                    timer = previousState.timer,
                                )
                            )
                        
                        spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u ->
                            CheckResult.Success(
                                GameStateMachine.State.RoundLastGuess(
                                    metadata = metadataTransformer(previousState, transition),
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
                                    roundNumber = previousState.roundNumber,
                                    cycleNumber = previousState.cycleNumber,
                                    speakerIndex = previousState.speakerIndex,
                                    listenerIndex = previousState.listenerIndex,
                                    startInstant = previousState.startInstant,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = previousState.restWords,
                                    currentWord = previousState.currentWord,
                                    explanationScores = previousState.explanationScores,
                                    guessingScores = previousState.guessingScores,
                                    currentExplanationResults = previousState.currentExplanationResults,
                                    timer = previousState.timer,
                                )
                            )
                        
                        else -> {
                            previousState.timer.cancel()
                            if (previousState.settings.strictMode)
                                CheckResult.Success(
                                    GameStateMachine.State.RoundEditing(
                                        metadata = metadataTransformer(previousState, transition),
                                        playersList = previousState.playersList,
                                        settings = previousState.settings,
                                        roundNumber = previousState.roundNumber,
                                        cycleNumber = previousState.cycleNumber,
                                        speakerIndex = previousState.speakerIndex,
                                        listenerIndex = previousState.listenerIndex,
                                        restWords = previousState.restWords,
                                        explanationScores = previousState.explanationScores,
                                        guessingScores = previousState.guessingScores,
                                        currentExplanationResults = KoneList(previousState.currentExplanationResults.size + 1u) {
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
                                        metadata = metadataTransformer(previousState, transition),
                                        playersList = previousState.playersList,
                                        settings = previousState.settings,
                                        roundNumber = previousState.roundNumber,
                                        cycleNumber = previousState.cycleNumber,
                                        speakerIndex = previousState.speakerIndex,
                                        listenerIndex = previousState.listenerIndex,
                                        startInstant = previousState.startInstant,
                                        millisecondsLeft = 0u,
                                        restWords = previousState.restWords,
                                        currentWord = previousState.currentWord,
                                        explanationScores = previousState.explanationScores,
                                        guessingScores = previousState.guessingScores,
                                        currentExplanationResults = previousState.currentExplanationResults,
                                        timer = previousState.timer,
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
                            previousState.timer.cancel()
                            CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                        }
                        spentTimeMilliseconds < (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u ->
                            CheckResult.Success(
                                GameStateMachine.State.RoundLastGuess(
                                    metadata = metadataTransformer(previousState, transition),
                                    playersList = previousState.playersList,
                                    settings = previousState.settings,
                                    roundNumber = previousState.roundNumber,
                                    cycleNumber = previousState.cycleNumber,
                                    speakerIndex = previousState.speakerIndex,
                                    listenerIndex = previousState.listenerIndex,
                                    startInstant = previousState.startInstant,
                                    millisecondsLeft = (preparationTimeSeconds + explanationTimeSeconds + finalGuessTimeSeconds) * 1000u - spentTimeMilliseconds,
                                    restWords = previousState.restWords,
                                    currentWord = previousState.currentWord,
                                    explanationScores = previousState.explanationScores,
                                    guessingScores = previousState.guessingScores,
                                    currentExplanationResults = previousState.currentExplanationResults,
                                    timer = previousState.timer,
                                )
                            )
                        
                        else -> {
                            previousState.timer.cancel()
                            if (previousState.settings.strictMode)
                                CheckResult.Success(
                                    GameStateMachine.State.RoundEditing(
                                        metadata = metadataTransformer(previousState, transition),
                                        playersList = previousState.playersList,
                                        settings = previousState.settings,
                                        roundNumber = previousState.roundNumber,
                                        cycleNumber = previousState.cycleNumber,
                                        speakerIndex = previousState.speakerIndex,
                                        listenerIndex = previousState.listenerIndex,
                                        restWords = previousState.restWords,
                                        explanationScores = previousState.explanationScores,
                                        guessingScores = previousState.guessingScores,
                                        currentExplanationResults = KoneList(previousState.currentExplanationResults.size + 1u) {
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
                                        metadata = metadataTransformer(previousState, transition),
                                        playersList = previousState.playersList,
                                        settings = previousState.settings,
                                        roundNumber = previousState.roundNumber,
                                        cycleNumber = previousState.cycleNumber,
                                        speakerIndex = previousState.speakerIndex,
                                        listenerIndex = previousState.listenerIndex,
                                        startInstant = previousState.startInstant,
                                        millisecondsLeft = 0u,
                                        restWords = previousState.restWords,
                                        currentWord = previousState.currentWord,
                                        explanationScores = previousState.explanationScores,
                                        guessingScores = previousState.guessingScores,
                                        currentExplanationResults = previousState.currentExplanationResults,
                                        timer = previousState.timer,
                                    )
                                )
                        }
                    }
                }
                is GameStateMachine.State.RoundEditing -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
                is GameStateMachine.State.GameResults -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateRoundInfoNotDuringTheRound)
            }
        is GameStateMachine.Transition.UpdateGame.WordExplanationState ->
            when (previousState) {
                is GameStateMachine.State.RoundExplanation ->
                    if (previousState.restWords.isNotEmpty() && transition.wordState == GameStateMachine.WordExplanation.State.Explained) {
                        val nextCurrentWord = previousState.restWords.random(Random)
                        val nextRestWords = previousState.restWords.filterTo(KoneMutableSet.of()) { it != nextCurrentWord }
                        CheckResult.Success(
                            GameStateMachine.State.RoundExplanation(
                                metadata = metadataTransformer(previousState, transition),
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                roundNumber = previousState.roundNumber,
                                cycleNumber = previousState.cycleNumber,
                                speakerIndex = previousState.speakerIndex,
                                listenerIndex = previousState.listenerIndex,
                                startInstant = previousState.startInstant,
                                millisecondsLeft = previousState.millisecondsLeft,
                                restWords = nextRestWords,
                                currentWord = nextCurrentWord,
                                explanationScores = previousState.explanationScores,
                                guessingScores = previousState.guessingScores,
                                currentExplanationResults = KoneList(previousState.currentExplanationResults.size + 1u) {
                                    when (it) {
                                        in previousState.currentExplanationResults.indices -> previousState.currentExplanationResults[it]
                                        else -> GameStateMachine.WordExplanation(previousState.currentWord, transition.wordState)
                                    }
                                },
                                timer = previousState.timer,
                            )
                        )
                    } else {
                        previousState.timer.cancel()
                        CheckResult.Success(
                            GameStateMachine.State.RoundEditing(
                                metadata = metadataTransformer(previousState, transition),
                                playersList = previousState.playersList,
                                settings = previousState.settings,
                                roundNumber = previousState.roundNumber,
                                cycleNumber = previousState.cycleNumber,
                                speakerIndex = previousState.speakerIndex,
                                listenerIndex = previousState.listenerIndex,
                                restWords = previousState.restWords,
                                explanationScores = previousState.explanationScores,
                                guessingScores = previousState.guessingScores,
                                currentExplanationResults = KoneList(previousState.currentExplanationResults.size + 1u) {
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
                    previousState.timer.cancel()
                    CheckResult.Success(
                        GameStateMachine.State.RoundEditing(
                            metadata = metadataTransformer(previousState, transition),
                            playersList = previousState.playersList,
                            settings = previousState.settings,
                            roundNumber = previousState.roundNumber,
                            cycleNumber = previousState.cycleNumber,
                            speakerIndex = previousState.speakerIndex,
                            listenerIndex = previousState.listenerIndex,
                            restWords = previousState.restWords,
                            explanationScores = previousState.explanationScores,
                            guessingScores = previousState.guessingScores,
                            currentExplanationResults = KoneList(previousState.currentExplanationResults.size + 1u) {
                                when (it) {
                                    in previousState.currentExplanationResults.indices -> previousState.currentExplanationResults[it]
                                    else -> GameStateMachine.WordExplanation(previousState.currentWord, transition.wordState)
                                }
                            },
                        )
                    )
                }
                
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.RoundWaiting,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess)
            }
        is GameStateMachine.Transition.UpdateGame.UpdateWordsExplanationResults ->
            when (previousState) {
                is GameStateMachine.State.RoundEditing ->
                    CheckResult.Success(
                        GameStateMachine.State.RoundEditing(
                            metadata = metadataTransformer(previousState, transition),
                            playersList = previousState.playersList,
                            settings = previousState.settings,
                            roundNumber = previousState.roundNumber,
                            cycleNumber = previousState.cycleNumber,
                            speakerIndex = previousState.speakerIndex,
                            listenerIndex = previousState.listenerIndex,
                            restWords = previousState.restWords,
                            explanationScores = previousState.explanationScores,
                            guessingScores = previousState.guessingScores,
                            currentExplanationResults = transition.newExplanationResults,
                        )
                    )
                
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.RoundWaiting,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotUpdateWordExplanationResultsNotDuringRoundEditing)
            }
        is GameStateMachine.Transition.UpdateGame.ConfirmWordsExplanationResults ->
            when (previousState) {
                is GameStateMachine.State.RoundEditing -> {
                    val newRestWords = KoneSet.build<String> {
                        this += previousState.restWords
                        this += previousState.currentExplanationResults.filter { it.state == GameStateMachine.WordExplanation.State.NotExplained }.map { it.word }
                    }
                    val numberOfExplainedWords = previousState.currentExplanationResults.count { it.state == GameStateMachine.WordExplanation.State.Explained }
                    val explanationScores = KoneList(previousState.playersList.size) { previousState.explanationScores[it] + if (it == previousState.speakerIndex) numberOfExplainedWords else 0u }
                    val guessingScores = KoneList(previousState.playersList.size) { previousState.guessingScores[it] + if (it == previousState.listenerIndex) numberOfExplainedWords else 0u }
                    val nextRoundNumber = previousState.roundNumber + 1u
                    var nextCycleNumber = previousState.cycleNumber
                    val nextSpeakerIndex = (previousState.speakerIndex + 1u) % previousState.playersList.size
                    var nextListenerIndex = (previousState.listenerIndex + 1u) % previousState.playersList.size
                    if (nextSpeakerIndex == 0u) {
                        nextListenerIndex++
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
                                metadata = metadataTransformer(previousState, transition),
                                playersList = previousState.playersList,
                                results = KoneSettableList(previousState.playersList.size) {
                                    GameStateMachine.GameResult(
                                        it,
                                        explanationScores[it],
                                        guessingScores[it],
                                        explanationScores[it] + guessingScores[it],
                                    )
                                }.apply { sortByDescending { it.sum } }
                            )
                        )
                    else
                        CheckResult.Success(
                            GameStateMachine.State.RoundWaiting(
                                metadata = metadataTransformer(previousState, transition),
                                playersList = previousState.playersList,
                                settings = previousState.settings,
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
                        )
                }
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.RoundWaiting,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotConfirmWordExplanationResultsNotDuringRoundEditing)
            }
        is GameStateMachine.Transition.UpdateGame.FinishGame ->
            when (previousState) {
                is GameStateMachine.State.RoundWaiting ->
                    CheckResult.Success(
                        GameStateMachine.State.GameResults(
                            metadata = metadataTransformer(previousState, transition),
                            playersList = previousState.playersList,
                            results = KoneSettableList(previousState.playersList.size) {
                                GameStateMachine.GameResult(
                                    player = it,
                                    scoreExplained = previousState.explanationScores[it],
                                    scoreGuessed = previousState.guessingScores[it],
                                    sum = previousState.explanationScores[it] + previousState.guessingScores[it],
                                )
                            }.apply { sortByDescending { it.sum } }
                        )
                    )
                is GameStateMachine.State.GameInitialisation,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> CheckResult.Failure(GameStateMachine.NoNextStateReason.CannotFinishGameNotDuringRoundWaiting)
            }
    }
    