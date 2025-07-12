package dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen

import dev.lounres.halfhat.client.common.logic.wordsProviders.DeviceGameWordsProviderID
import dev.lounres.halfhat.client.common.logic.wordsProviders.NoDeviceGameWordsProviderReason
import dev.lounres.halfhat.client.common.logic.wordsProviders.deviceGameWordsProviderRegistry
import dev.lounres.halfhat.client.common.utils.DefaultSounds
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultSlot
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.gameResults.RealGameResultsComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.loading.RealLoadingComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundEditing.RealRoundEditingComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundExplanation.RealRoundExplanationComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundLastGuess.RealRoundLastGuessComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundPreparation.RealRoundPreparationComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundWaiting.RealRoundWaitingComponent
import dev.lounres.halfhat.client.common.utils.play
import dev.lounres.halfhat.client.components.logger.logger
import dev.lounres.halfhat.logic.gameStateMachine.*
import dev.lounres.komponentual.navigation.ChildrenSlot
import dev.lounres.komponentual.navigation.MutableSlotNavigation
import dev.lounres.kone.automata.CheckResult
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.toKoneMutableList
import dev.lounres.kone.state.KoneAsynchronousState
import dev.lounres.logKube.core.debug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random


public class RealGameScreenComponent(
    override val onExitGame: () -> Unit,
    override val childSlot: KoneAsynchronousState<ChildrenSlot<*, GameScreenComponent.Child>>,
) : GameScreenComponent {
    public sealed interface Configuration {
        public data object GameInitialisation : Configuration
        public data class RoundWaiting(
            val speaker: MutableStateFlow<String>,
            val listener: MutableStateFlow<String>,
        ) : Configuration
        public data class RoundPreparation(
            val speaker: MutableStateFlow<String>,
            val listener: MutableStateFlow<String>,
            val millisecondsLeft: MutableStateFlow<UInt>,
        ) : Configuration
        public data class RoundExplanation(
            val speaker: MutableStateFlow<String>,
            val listener: MutableStateFlow<String>,
            val millisecondsLeft: MutableStateFlow<UInt>,
            val word: MutableStateFlow<String>,
        ) : Configuration
        public data class RoundLastGuess(
            val speaker: MutableStateFlow<String>,
            val listener: MutableStateFlow<String>,
            val millisecondsLeft: MutableStateFlow<UInt>,
            val word: MutableStateFlow<String>,
        ) : Configuration
        public data class RoundEditing(
            val wordsToEdit: MutableStateFlow<KoneList<GameStateMachine.WordExplanation>>,
        ) : Configuration
        public data class GameResults(
            val results: MutableStateFlow<KoneList<GameStateMachine.PersonalResult<String>>>,
        ) : Configuration
    }
}

public suspend fun RealGameScreenComponent(
    componentContext: UIComponentContext,
    volumeOn: StateFlow<Boolean>,
    playersList: KoneList<String>,
    settingsBuilder: GameStateMachine.GameSettings.Builder<DeviceGameWordsProviderID>,
    onExitGame: () -> Unit,
): RealGameScreenComponent {
    val logger = componentContext.logger
    val coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    val navigation = MutableSlotNavigation<RealGameScreenComponent.Configuration>()
    
    val gameStateMachine = AsynchronousGameStateMachine.Initialization<String, DeviceGameWordsProviderID, NoDeviceGameWordsProviderReason, Nothing?, Nothing?, Nothing?>(
        metadata = null,
        playersList = playersList,
        settingsBuilder = settingsBuilder,
        coroutineScope = coroutineScope,
        random = Random, // TODO: Move the variable upward
        checkMetadataUpdate = { _, _ -> CheckResult.Failure(null) }
    ) { previousState, transition, newState ->
        logger.debug(
            source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
            items = {
                mapOf(
                    "previousState" to previousState.toString(),
                    "transition" to transition.toString(),
                    "newState" to newState.toString(),
                )
            }
        ) { "Game state machine transition started" }
        if (volumeOn.value) when (newState) {
            is GameStateMachine.State.GameInitialisation ->
                when (previousState) {
                    is GameStateMachine.State.GameInitialisation -> {}
                    is GameStateMachine.State.RoundWaiting -> {}
                    is GameStateMachine.State.RoundPreparation ->
                        coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundExplanation ->
                        coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundLastGuess ->
                        if (previousState.millisecondsLeft > 0u)
                            coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundEditing -> {}
                    is GameStateMachine.State.GameResults -> {}
                }
            is GameStateMachine.State.RoundWaiting ->
                when (previousState) {
                    is GameStateMachine.State.GameInitialisation -> {}
                    is GameStateMachine.State.RoundWaiting -> {}
                    is GameStateMachine.State.RoundPreparation ->
                        coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundExplanation ->
                        coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundLastGuess ->
                        if (previousState.millisecondsLeft > 0u)
                            coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundEditing -> {}
                    is GameStateMachine.State.GameResults -> {}
                }
            is GameStateMachine.State.RoundPreparation ->
                when (previousState) {
                    is GameStateMachine.State.GameInitialisation ->
                        coroutineScope.launch { DefaultSounds.preparationCountdown.await().play() }
                    is GameStateMachine.State.RoundWaiting ->
                        coroutineScope.launch { DefaultSounds.preparationCountdown.await().play() }
                    is GameStateMachine.State.RoundPreparation ->
                        if (previousState.roundNumber != newState.roundNumber || (previousState.millisecondsLeft / 1000u) != (newState.millisecondsLeft / 1000u))
                            coroutineScope.launch { DefaultSounds.preparationCountdown.await().play() }
                    is GameStateMachine.State.RoundExplanation ->
                        coroutineScope.launch { DefaultSounds.preparationCountdown.await().play() }
                    is GameStateMachine.State.RoundLastGuess ->
                        coroutineScope.launch { DefaultSounds.preparationCountdown.await().play() }
                    is GameStateMachine.State.RoundEditing ->
                        coroutineScope.launch { DefaultSounds.preparationCountdown.await().play() }
                    is GameStateMachine.State.GameResults ->
                        coroutineScope.launch { DefaultSounds.preparationCountdown.await().play() }
                }
            is GameStateMachine.State.RoundExplanation ->
                when (previousState) {
                    is GameStateMachine.State.GameInitialisation ->
                        coroutineScope.launch { DefaultSounds.explanationStart.await().play() }
                    is GameStateMachine.State.RoundWaiting ->
                        coroutineScope.launch { DefaultSounds.explanationStart.await().play() }
                    is GameStateMachine.State.RoundPreparation ->
                        coroutineScope.launch { DefaultSounds.explanationStart.await().play() }
                    is GameStateMachine.State.RoundExplanation ->
                        if (previousState.roundNumber != newState.roundNumber)
                            coroutineScope.launch { DefaultSounds.explanationStart.await().play() }
                    is GameStateMachine.State.RoundLastGuess ->
                        coroutineScope.launch { DefaultSounds.explanationStart.await().play() }
                    is GameStateMachine.State.RoundEditing ->
                        coroutineScope.launch { DefaultSounds.explanationStart.await().play() }
                    is GameStateMachine.State.GameResults ->
                        coroutineScope.launch { DefaultSounds.explanationStart.await().play() }
                }
            is GameStateMachine.State.RoundLastGuess ->
                when (previousState) {
                    is GameStateMachine.State.GameInitialisation ->
                        if (newState.millisecondsLeft > 0u)
                            coroutineScope.launch { DefaultSounds.finalGuessStart.await().play() }
                        else
                            coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundWaiting ->
                        if (newState.millisecondsLeft > 0u)
                            coroutineScope.launch { DefaultSounds.finalGuessStart.await().play() }
                        else
                            coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundPreparation ->
                        if (newState.millisecondsLeft > 0u)
                            coroutineScope.launch { DefaultSounds.finalGuessStart.await().play() }
                        else
                            coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundExplanation ->
                        if (newState.millisecondsLeft > 0u)
                            coroutineScope.launch { DefaultSounds.finalGuessStart.await().play() }
                        else
                            coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundLastGuess ->
                        if (newState.millisecondsLeft == 0u) {
                            if (newState.roundNumber != previousState.roundNumber)
                                coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                        } else {
                            if (newState.roundNumber != previousState.roundNumber)
                                coroutineScope.launch { DefaultSounds.finalGuessStart.await().play() }
                        }
                    is GameStateMachine.State.RoundEditing ->
                        if (newState.millisecondsLeft > 0u)
                            coroutineScope.launch { DefaultSounds.finalGuessStart.await().play() }
                        else
                            coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.GameResults ->
                        if (newState.millisecondsLeft > 0u)
                            coroutineScope.launch { DefaultSounds.finalGuessStart.await().play() }
                        else
                            coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                }
            is GameStateMachine.State.RoundEditing ->
                when (previousState) {
                    is GameStateMachine.State.GameInitialisation -> {}
                    is GameStateMachine.State.RoundWaiting -> {}
                    is GameStateMachine.State.RoundPreparation ->
                        coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundExplanation ->
                        coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundLastGuess ->
                        if (previousState.millisecondsLeft > 0u)
                            coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundEditing -> {}
                    is GameStateMachine.State.GameResults -> {}
                }
            is GameStateMachine.State.GameResults ->
                when (previousState) {
                    is GameStateMachine.State.GameInitialisation -> {}
                    is GameStateMachine.State.RoundWaiting -> {}
                    is GameStateMachine.State.RoundPreparation ->
                        coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundExplanation ->
                        coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundLastGuess ->
                        if (previousState.millisecondsLeft > 0u)
                            coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                    is GameStateMachine.State.RoundEditing -> {}
                    is GameStateMachine.State.GameResults -> {}
                }
        }
        navigation.navigate { currentConfiguration ->
            when (newState) {
                is GameStateMachine.State.GameInitialisation ->
                    RealGameScreenComponent.Configuration.GameInitialisation
                is GameStateMachine.State.RoundWaiting ->
                    if (currentConfiguration is RealGameScreenComponent.Configuration.RoundWaiting)
                        currentConfiguration.apply {
                            speaker.value = newState.speaker
                            listener.value = newState.listener
                        }
                    else
                        RealGameScreenComponent.Configuration.RoundWaiting(
                            speaker = MutableStateFlow(newState.speaker),
                            listener = MutableStateFlow(newState.listener),
                        )
                
                is GameStateMachine.State.RoundPreparation ->
                    if (currentConfiguration is RealGameScreenComponent.Configuration.RoundPreparation)
                        currentConfiguration.apply {
                            speaker.value = newState.speaker
                            listener.value = newState.listener
                            millisecondsLeft.value = newState.millisecondsLeft
                        }
                    else
                        RealGameScreenComponent.Configuration.RoundPreparation(
                            speaker = MutableStateFlow(newState.speaker),
                            listener = MutableStateFlow(newState.listener),
                            millisecondsLeft = MutableStateFlow(newState.millisecondsLeft),
                        )
                
                is GameStateMachine.State.RoundExplanation ->
                    if (currentConfiguration is RealGameScreenComponent.Configuration.RoundExplanation)
                        currentConfiguration.apply {
                            speaker.value = newState.speaker
                            listener.value = newState.listener
                            millisecondsLeft.value = newState.millisecondsLeft
                            word.value = newState.currentWord
                        }
                    else
                        RealGameScreenComponent.Configuration.RoundExplanation(
                            speaker = MutableStateFlow(newState.speaker),
                            listener = MutableStateFlow(newState.listener),
                            millisecondsLeft = MutableStateFlow(newState.millisecondsLeft),
                            word = MutableStateFlow(newState.currentWord),
                        )
                
                is GameStateMachine.State.RoundLastGuess ->
                    if (currentConfiguration is RealGameScreenComponent.Configuration.RoundLastGuess)
                        currentConfiguration.apply {
                            speaker.value = newState.speaker
                            listener.value = newState.listener
                            millisecondsLeft.value = newState.millisecondsLeft
                            word.value = newState.currentWord
                        }
                    else
                        RealGameScreenComponent.Configuration.RoundLastGuess(
                            speaker = MutableStateFlow(newState.speaker),
                            listener = MutableStateFlow(newState.listener),
                            millisecondsLeft = MutableStateFlow(newState.millisecondsLeft),
                            word = MutableStateFlow(newState.currentWord),
                        )
                
                is GameStateMachine.State.RoundEditing ->
                    if (currentConfiguration is RealGameScreenComponent.Configuration.RoundEditing)
                        currentConfiguration.apply {
                            wordsToEdit.value = newState.currentExplanationResults
                        }
                    else
                        RealGameScreenComponent.Configuration.RoundEditing(
                            wordsToEdit = MutableStateFlow(newState.currentExplanationResults),
                        )
                
                is GameStateMachine.State.GameResults ->
                    if (currentConfiguration is RealGameScreenComponent.Configuration.GameResults)
                        currentConfiguration.apply {
                            results.value = newState.personalResults
                        }
                    else
                        RealGameScreenComponent.Configuration.GameResults(
                            results = MutableStateFlow(newState.personalResults),
                        )
            }
        }
        logger.debug(
            source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
            items = {
                mapOf(
                    "previousState" to previousState.toString(),
                    "transition" to transition.toString(),
                    "newState" to newState.toString(),
                )
            }
        ) { "Game state machine transition ended" }
    }
    
    // TODO: Make the state machine initialized
    
    gameStateMachine.initialiseGame(
        wordsProviderRegistry = componentContext.deviceGameWordsProviderRegistry,
    )
    
    val childSlot: KoneAsynchronousState<ChildrenSlot<RealGameScreenComponent.Configuration, GameScreenComponent.Child>> =
        componentContext.uiChildrenDefaultSlot(
            loggerSource = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
            source = navigation,
            initialConfiguration = when(val state = gameStateMachine.state) {
                is GameStateMachine.State.GameInitialisation<*, *, *> ->
                    RealGameScreenComponent.Configuration.GameInitialisation
                is GameStateMachine.State.RoundWaiting<String, *, *> ->
                    RealGameScreenComponent.Configuration.RoundWaiting(
                        speaker = MutableStateFlow(state.speaker),
                        listener = MutableStateFlow(state.listener),
                    )
                is GameStateMachine.State.RoundPreparation<String, *, *> ->
                    RealGameScreenComponent.Configuration.RoundPreparation(
                        speaker = MutableStateFlow(state.speaker),
                        listener = MutableStateFlow(state.listener),
                        millisecondsLeft = MutableStateFlow(state.millisecondsLeft),
                    )
                is GameStateMachine.State.RoundExplanation<String, *, *> ->
                    RealGameScreenComponent.Configuration.RoundExplanation(
                        speaker = MutableStateFlow(state.speaker),
                        listener = MutableStateFlow(state.listener),
                        millisecondsLeft = MutableStateFlow(state.millisecondsLeft),
                        word = MutableStateFlow(state.currentWord),
                    )
                is GameStateMachine.State.RoundLastGuess<String, *, *> ->
                    RealGameScreenComponent.Configuration.RoundLastGuess(
                        speaker = MutableStateFlow(state.speaker),
                        listener = MutableStateFlow(state.listener),
                        millisecondsLeft = MutableStateFlow(state.millisecondsLeft),
                        word = MutableStateFlow(state.currentWord),
                    )
                is GameStateMachine.State.RoundEditing<String, *, *> ->
                    RealGameScreenComponent.Configuration.RoundEditing(
                        wordsToEdit = MutableStateFlow(state.currentExplanationResults),
                    )
                is GameStateMachine.State.GameResults<String, *> ->
                    RealGameScreenComponent.Configuration.GameResults(
                        results = MutableStateFlow(state.personalResults),
                    )
            },
        ) { configuration, _ ->
            when(configuration) {
                RealGameScreenComponent.Configuration.GameInitialisation ->
                    GameScreenComponent.Child.Loading(
                        RealLoadingComponent(
                            onExitGame = onExitGame,
                        )
                    )
                is RealGameScreenComponent.Configuration.RoundWaiting ->
                    GameScreenComponent.Child.RoundWaiting(
                        RealRoundWaitingComponent(
                            onExitGame = onExitGame,
                            onFinishGame = {
                                coroutineScope.launch {
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                    ) { "Finishing game" }
                                    val result = gameStateMachine.finishGame()
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                        items = {
                                            mapOf(
                                                "result" to result.toString(),
                                            )
                                        },
                                    ) {
                                        "Finished game"
                                    }
                                }
                            },
                            speaker = configuration.speaker,
                            listener = configuration.listener,
                            onStartRound = {
                                coroutineScope.launch {
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                    ) { "Making speaker and listener ready" }
                                    val result = gameStateMachine.speakerAndListenerReady()
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                        items = {
                                            mapOf(
                                                "result" to result.toString(),
                                            )
                                        },
                                    ) { "Made speaker and listener ready" }
                                }
                            }
                        )
                    )
                is RealGameScreenComponent.Configuration.RoundPreparation ->
                    GameScreenComponent.Child.RoundPreparation(
                        RealRoundPreparationComponent(
                            onExitGame = onExitGame,
                            speaker = configuration.speaker,
                            listener = configuration.listener,
                            millisecondsLeft = configuration.millisecondsLeft,
                        )
                    )
                is RealGameScreenComponent.Configuration.RoundExplanation ->
                    GameScreenComponent.Child.RoundExplanation(
                        RealRoundExplanationComponent(
                            onExitGame = onExitGame,
                            speaker = configuration.speaker,
                            listener = configuration.listener,
                            millisecondsLeft = configuration.millisecondsLeft,
                            word = configuration.word,
                            onGuessed = {
                                coroutineScope.launch {
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                    ) { "Setting word explanation state to 'explained'" }
                                    val result = gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.Explained)
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                        items = {
                                            mapOf(
                                                "result" to result.toString(),
                                            )
                                        },
                                    ) { "Set word explanation state to 'explained'" }
                                }
                            },
                            onNotGuessed = {
                                coroutineScope.launch {
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                    ) { "Setting word explanation state to 'not explained'" }
                                    val result = gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.NotExplained)
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                        items = {
                                            mapOf(
                                                "result" to result.toString(),
                                            )
                                        },
                                    ) { "Set word explanation state to 'not explained'" }
                                }
                            },
                            onMistake = {
                                coroutineScope.launch {
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                    ) { "Setting word explanation state to 'mistake'" }
                                    val result = gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.Mistake)
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                        items = {
                                            mapOf(
                                                "result" to result.toString(),
                                            )
                                        },
                                    ) { "Set word explanation state to 'mistake'" }
                                }
                            },
                        )
                    )
                is RealGameScreenComponent.Configuration.RoundLastGuess ->
                    GameScreenComponent.Child.RoundLastGuess(
                        RealRoundLastGuessComponent(
                            onExitGame = onExitGame,
                            speaker = configuration.speaker,
                            listener = configuration.listener,
                            millisecondsLeft = configuration.millisecondsLeft,
                            word = configuration.word,
                            onGuessed = {
                                coroutineScope.launch {
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                    ) { "Setting word explanation state to 'explained'" }
                                    val result = gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.Explained)
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                        items = {
                                            mapOf(
                                                "result" to result.toString(),
                                            )
                                        },
                                    ) { "Set word explanation state to 'explained'" }
                                }
                            },
                            onNotGuessed = {
                                coroutineScope.launch {
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                    ) { "Setting word explanation state to 'not explained'" }
                                    val result = gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.NotExplained)
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                        items = {
                                            mapOf(
                                                "result" to result.toString(),
                                            )
                                        },
                                    ) { "Set word explanation state to 'not explained'" }
                                }
                            },
                            onMistake = {
                                coroutineScope.launch {
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                    ) { "Setting word explanation state to 'mistake'" }
                                    val result = gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.Mistake)
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                        items = {
                                            mapOf(
                                                "result" to result.toString(),
                                            )
                                        },
                                    ) { "Set word explanation state to 'mistake'" }
                                }
                            },
                        )
                    )
                is RealGameScreenComponent.Configuration.RoundEditing ->
                    GameScreenComponent.Child.RoundEditing(
                        RealRoundEditingComponent(
                            onExitGame = onExitGame,
                            wordsToEdit = configuration.wordsToEdit,
                            onGuessed = { index ->
                                coroutineScope.launch {
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                    ) { "Updating word explanation results" }
                                    val result = gameStateMachine.updateWordsExplanationResults(
                                        configuration.wordsToEdit.value.toKoneMutableList().apply {
                                            this[index] = GameStateMachine.WordExplanation(
                                                this[index].word,
                                                GameStateMachine.WordExplanation.State.Explained
                                            )
                                        }
                                    )
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                        items = {
                                            mapOf(
                                                "result" to result.toString(),
                                            )
                                        },
                                    ) { "Updated word explanation results" }
                                }
                            },
                            onNotGuessed = { index ->
                                coroutineScope.launch {
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                    ) { "Updating word explanation results" }
                                    val result = gameStateMachine.updateWordsExplanationResults(
                                        configuration.wordsToEdit.value.toKoneMutableList().apply {
                                            this[index] = GameStateMachine.WordExplanation(
                                                this[index].word,
                                                GameStateMachine.WordExplanation.State.NotExplained
                                            )
                                        }
                                    )
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                        items = {
                                            mapOf(
                                                "result" to result.toString(),
                                            )
                                        },
                                    ) { "Updated word explanation results" }
                                }
                            },
                            onMistake = { index ->
                                coroutineScope.launch {
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                    ) { "Updating word explanation results" }
                                    val result = gameStateMachine.updateWordsExplanationResults(
                                        configuration.wordsToEdit.value.toKoneMutableList().apply {
                                            this[index] = GameStateMachine.WordExplanation(
                                                this[index].word,
                                                GameStateMachine.WordExplanation.State.Mistake
                                            )
                                        }
                                    )
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                        items = {
                                            mapOf(
                                                "result" to result.toString(),
                                            )
                                        },
                                    ) { "Updated word explanation results" }
                                }
                            },
                            onConfirm = {
                                coroutineScope.launch {
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                    ) { "Confirming word explanation results" }
                                    val result = gameStateMachine.confirmWordsExplanationResults()
                                    logger.debug(
                                        source = "dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent",
                                        items = {
                                            mapOf(
                                                "result" to result.toString(),
                                            )
                                        },
                                    ) { "Confirmed word explanation results" }
                                }
                            }
                        )
                    )
                is RealGameScreenComponent.Configuration.GameResults ->
                    GameScreenComponent.Child.GameResults(
                        RealGameResultsComponent(
                            onExitGame = onExitGame,
                            results = configuration.results
                        )
                    )
            }
        }
    
    return RealGameScreenComponent(
        onExitGame = onExitGame,
        childSlot = childSlot,
    )
}