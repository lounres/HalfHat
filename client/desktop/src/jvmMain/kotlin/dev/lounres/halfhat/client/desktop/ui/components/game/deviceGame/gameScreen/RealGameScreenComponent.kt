package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultSlot
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.gameResults.RealGameResultsComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundEditing.RealRoundEditingComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundExplanation.RealRoundExplanationComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundLastGuess.RealRoundLastGuessComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundPreparation.RealRoundPreparationComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundWaiting.RealRoundWaitingComponent
import dev.lounres.halfhat.logic.gameStateMachine.*
import dev.lounres.komponentual.navigation.ChildrenSlot
import dev.lounres.komponentual.navigation.MutableSlotNavigation
import dev.lounres.kone.automata.CheckResult
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.toKoneMutableList
import dev.lounres.kone.maybe.None
import dev.lounres.kone.state.KoneState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random


class RealGameScreenComponent(
    componentContext: UIComponentContext,
    playersList: KoneList<String>,
    settingsBuilder: GameStateMachine.GameSettings.Builder<GameStateMachine.WordsProvider>,
    override val onExitGame: () -> Unit,
) : GameScreenComponent {
    
    private val coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    
    private val gameStateMachine = BlockingGameStateMachine.Initialized<String, GameStateMachine.WordsProvider, Nothing?, Nothing?, Nothing?>(
        metadata = null,
        playersList = playersList,
        settingsBuilder = settingsBuilder,
        coroutineScope = coroutineScope,
        random = Random, // TODO: Move the variable upward
        checkMetadataUpdate = { _, _ -> CheckResult.Failure(null) }
    ) { _, _, newState ->
        navigation.navigate { currentConfiguration ->
            when (newState) {
                is GameStateMachine.State.GameInitialisation -> error("GameInitialisation appeared after initialization")
                is GameStateMachine.State.RoundWaiting ->
                    if (currentConfiguration is Configuration.RoundWaiting)
                        currentConfiguration.apply {
                            speaker.value = newState.speaker
                            listener.value = newState.listener
                        }
                    else
                        Configuration.RoundWaiting(
                            speaker = MutableStateFlow(newState.speaker),
                            listener = MutableStateFlow(newState.listener),
                        )
                
                is GameStateMachine.State.RoundPreparation ->
                    if (currentConfiguration is Configuration.RoundPreparation)
                        currentConfiguration.apply {
                            speaker.value = newState.speaker
                            listener.value = newState.listener
                            millisecondsLeft.value = newState.millisecondsLeft
                        }
                    else
                        Configuration.RoundPreparation(
                            speaker = MutableStateFlow(newState.speaker),
                            listener = MutableStateFlow(newState.listener),
                            millisecondsLeft = MutableStateFlow(newState.millisecondsLeft),
                        )
                
                is GameStateMachine.State.RoundExplanation ->
                    if (currentConfiguration is Configuration.RoundExplanation)
                        currentConfiguration.apply {
                            speaker.value = newState.speaker
                            listener.value = newState.listener
                            millisecondsLeft.value = newState.millisecondsLeft
                            word.value = newState.currentWord
                        }
                    else
                        Configuration.RoundExplanation(
                            speaker = MutableStateFlow(newState.speaker),
                            listener = MutableStateFlow(newState.listener),
                            millisecondsLeft = MutableStateFlow(newState.millisecondsLeft),
                            word = MutableStateFlow(newState.currentWord),
                        )
                
                is GameStateMachine.State.RoundLastGuess ->
                    if (currentConfiguration is Configuration.RoundLastGuess)
                        currentConfiguration.apply {
                            speaker.value = newState.speaker
                            listener.value = newState.listener
                            millisecondsLeft.value = newState.millisecondsLeft
                            word.value = newState.currentWord
                        }
                    else
                        Configuration.RoundLastGuess(
                            speaker = MutableStateFlow(newState.speaker),
                            listener = MutableStateFlow(newState.listener),
                            millisecondsLeft = MutableStateFlow(newState.millisecondsLeft),
                            word = MutableStateFlow(newState.currentWord),
                        )
                
                is GameStateMachine.State.RoundEditing ->
                    if (currentConfiguration is Configuration.RoundEditing)
                        currentConfiguration.apply {
                            wordsToEdit.value = newState.currentExplanationResults
                        }
                    else
                        Configuration.RoundEditing(
                            wordsToEdit = MutableStateFlow(newState.currentExplanationResults),
                        )
                
                is GameStateMachine.State.GameResults ->
                    if (currentConfiguration is Configuration.GameResults)
                        currentConfiguration.apply {
                            results.value = newState.personalResults
                        }
                    else
                        Configuration.GameResults(
                            results = MutableStateFlow(newState.personalResults),
                        )
            }
        }
        
    }
    
    private val navigation = MutableSlotNavigation<Configuration>()
    
    override val childStack: KoneState<ChildrenSlot<Configuration, GameScreenComponent.Child>> =
        componentContext.uiChildrenDefaultSlot<Configuration, _>(
            source = navigation,
            initialConfiguration = {
                when(val state = gameStateMachine.state) {
                    is GameStateMachine.State.GameInitialisation<*, *, *> ->
                        error("For some reason, game initialisation was reached in initialised game")
                    is GameStateMachine.State.RoundWaiting<String, *, *> ->
                        Configuration.RoundWaiting(
                            speaker = MutableStateFlow(state.speaker),
                            listener = MutableStateFlow(state.listener),
                        )
                    is GameStateMachine.State.RoundPreparation<String, *, *> ->
                        Configuration.RoundPreparation(
                            speaker = MutableStateFlow(state.speaker),
                            listener = MutableStateFlow(state.listener),
                            millisecondsLeft = MutableStateFlow(state.millisecondsLeft),
                        )
                    is GameStateMachine.State.RoundExplanation<String, *, *> ->
                        Configuration.RoundExplanation(
                            speaker = MutableStateFlow(state.speaker),
                            listener = MutableStateFlow(state.listener),
                            millisecondsLeft = MutableStateFlow(state.millisecondsLeft),
                            word = MutableStateFlow(state.currentWord),
                        )
                    is GameStateMachine.State.RoundLastGuess<String, *, *> ->
                        Configuration.RoundLastGuess(
                            speaker = MutableStateFlow(state.speaker),
                            listener = MutableStateFlow(state.listener),
                            millisecondsLeft = MutableStateFlow(state.millisecondsLeft),
                            word = MutableStateFlow(state.currentWord),
                        )
                    is GameStateMachine.State.RoundEditing<String, *, *> ->
                        Configuration.RoundEditing(
                            wordsToEdit = MutableStateFlow(state.currentExplanationResults),
                        )
                    is GameStateMachine.State.GameResults<String, *> ->
                        Configuration.GameResults(
                            results = MutableStateFlow(state.personalResults),
                        )
                }
            },
        ) { configuration, _ ->
            when(configuration) {
                is Configuration.RoundWaiting ->
                    GameScreenComponent.Child.RoundWaiting(
                        RealRoundWaitingComponent(
                            onExitGame = onExitGame,
                            onFinishGame = {
                                gameStateMachine.finishGame()
                            },
                            speaker = configuration.speaker,
                            listener = configuration.listener,
                            onStartRound = {
                                gameStateMachine.speakerAndListenerReady()
                            }
                        )
                    )
                is Configuration.RoundPreparation ->
                    GameScreenComponent.Child.RoundPreparation(
                        RealRoundPreparationComponent(
                            onExitGame = onExitGame,
                            speaker = configuration.speaker,
                            listener = configuration.listener,
                            millisecondsLeft = configuration.millisecondsLeft,
                        )
                    )
                is Configuration.RoundExplanation ->
                    GameScreenComponent.Child.RoundExplanation(
                        RealRoundExplanationComponent(
                            onExitGame = onExitGame,
                            speaker = configuration.speaker,
                            listener = configuration.listener,
                            millisecondsLeft = configuration.millisecondsLeft,
                            word = configuration.word,
                            onGuessed = {
                                gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.Explained)
                            },
                            onNotGuessed = {
                                gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.NotExplained)
                            },
                            onMistake = {
                                gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.Mistake)
                            },
                        )
                    )
                is Configuration.RoundLastGuess ->
                    GameScreenComponent.Child.RoundLastGuess(
                        RealRoundLastGuessComponent(
                            onExitGame = onExitGame,
                            speaker = configuration.speaker,
                            listener = configuration.listener,
                            millisecondsLeft = configuration.millisecondsLeft,
                            word = configuration.word,
                            onGuessed = {
                                gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.Explained)
                            },
                            onNotGuessed = {
                                gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.NotExplained)
                            },
                            onMistake = {
                                gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.Mistake)
                            },
                        )
                    )
                is Configuration.RoundEditing ->
                    GameScreenComponent.Child.RoundEditing(
                        RealRoundEditingComponent(
                            onExitGame = onExitGame,
                            wordsToEdit = configuration.wordsToEdit,
                            onGuessed = { index ->
                                gameStateMachine.updateWordsExplanationResults(
                                    configuration.wordsToEdit.value.toKoneMutableList().apply {
                                        this[index] = GameStateMachine.WordExplanation(this[index].word, GameStateMachine.WordExplanation.State.Explained)
                                    }
                                )
                            },
                            onNotGuessed = { index ->
                                gameStateMachine.updateWordsExplanationResults(
                                    configuration.wordsToEdit.value.toKoneMutableList().apply {
                                        this[index] = GameStateMachine.WordExplanation(this[index].word, GameStateMachine.WordExplanation.State.NotExplained)
                                    }
                                )
                            },
                            onMistake = { index ->
                                gameStateMachine.updateWordsExplanationResults(
                                    configuration.wordsToEdit.value.toKoneMutableList().apply {
                                        this[index] = GameStateMachine.WordExplanation(this[index].word, GameStateMachine.WordExplanation.State.Mistake)
                                    }
                                )
                            },
                            onConfirm = {
                                gameStateMachine.confirmWordsExplanationResults()
                            }
                        )
                    )
                is Configuration.GameResults ->
                    GameScreenComponent.Child.GameResults(
                        RealGameResultsComponent(
                            onExitGame = onExitGame,
                            results = configuration.results
                        )
                    )
            }
        }
    
    sealed interface Configuration {
        data class RoundWaiting(
            val speaker: MutableStateFlow<String>,
            val listener: MutableStateFlow<String>,
        ) : Configuration
        data class RoundPreparation(
            val speaker: MutableStateFlow<String>,
            val listener: MutableStateFlow<String>,
            val millisecondsLeft: MutableStateFlow<UInt>,
        ) : Configuration
        data class RoundExplanation(
            val speaker: MutableStateFlow<String>,
            val listener: MutableStateFlow<String>,
            val millisecondsLeft: MutableStateFlow<UInt>,
            val word: MutableStateFlow<String>,
        ) : Configuration
        data class RoundLastGuess(
            val speaker: MutableStateFlow<String>,
            val listener: MutableStateFlow<String>,
            val millisecondsLeft: MutableStateFlow<UInt>,
            val word: MutableStateFlow<String>,
        ) : Configuration
        data class RoundEditing(
            val wordsToEdit: MutableStateFlow<KoneList<GameStateMachine.WordExplanation>>,
        ) : Configuration
        data class GameResults(
            val results: MutableStateFlow<KoneList<GameStateMachine.PersonalResult<String>>>,
        ) : Configuration
    }
}