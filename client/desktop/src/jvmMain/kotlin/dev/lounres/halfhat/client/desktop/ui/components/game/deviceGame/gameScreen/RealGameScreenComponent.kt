package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.lounres.halfhat.client.common.ui.utils.updateCurrent
import dev.lounres.halfhat.client.common.utils.runOnUiThread
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.gameResults.RealGameResultsComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.loading.RealLoadingComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundEditing.RealRoundEditingComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundExplanation.RealRoundExplanationComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundLastGuess.RealRoundLastGuessComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundPreparation.RealRoundPreparationComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundWaiting.RealRoundWaitingComponent
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.halfhat.logic.gameStateMachine.listener
import dev.lounres.halfhat.logic.gameStateMachine.personalResults
import dev.lounres.halfhat.logic.gameStateMachine.speaker
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.toKoneMutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random


class RealGameScreenComponent(
    componentContext: ComponentContext,
    playersList: KoneList<String>,
    settingsBuilder: GameStateMachine.GameSettingsBuilder<GameStateMachine.WordsProvider>,
    override val onExitGame: () -> Unit,
) : GameScreenComponent {
    
    private val coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    private val structuralMutex = Mutex()
    
    private val gameStateMachine = GameStateMachine.Initialized<String, GameStateMachine.WordsProvider>(
        playersList = playersList,
        settingsBuilder = settingsBuilder,
        coroutineScope = coroutineScope,
        structuralMutex = structuralMutex,
        random = Random, // TODO: Move the variable upward
    )
    
    private val navigation = StackNavigation<Configuration>()
    
    override val childStack: Value<ChildStack<Configuration, GameScreenComponent.Child>> =
        componentContext.childStack(
            source = navigation,
            serializer = null,
            initialConfiguration = Configuration.Loading,
            handleBackButton = false,
        ) { configuration, componentContext ->
            when(configuration) {
                Configuration.Loading ->
                    GameScreenComponent.Child.Loading(
                        RealLoadingComponent(
                            onExitGame = onExitGame,
                        )
                    )
                is Configuration.RoundWaiting ->
                    GameScreenComponent.Child.RoundWaiting(
                        RealRoundWaitingComponent(
                            onExitGame = onExitGame,
                            onFinishGame = {
                                coroutineScope.launch {
                                    structuralMutex.withLock {
                                        gameStateMachine.finishGame()
                                    }
                                }
                            },
                            speaker = configuration.speaker,
                            listener = configuration.listener,
                            onStartRound = {
                                gameStateMachine.speakerReady()
                                gameStateMachine.listenerReady()
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
                                coroutineScope.launch {
                                    structuralMutex.withLock {
                                        gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.Explained)
                                    }
                                }
                            },
                            onNotGuessed = {
                                coroutineScope.launch {
                                    structuralMutex.withLock {
                                        gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.NotExplained)
                                    }
                                }
                            },
                            onMistake = {
                                coroutineScope.launch {
                                    structuralMutex.withLock {
                                        gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.Mistake)
                                    }
                                }
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
                                coroutineScope.launch {
                                    structuralMutex.withLock {
                                        gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.Explained)
                                    }
                                }
                            },
                            onNotGuessed = {
                                coroutineScope.launch {
                                    structuralMutex.withLock {
                                        gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.NotExplained)
                                    }
                                }
                            },
                            onMistake = {
                                coroutineScope.launch {
                                    structuralMutex.withLock {
                                        gameStateMachine.wordExplanationState(GameStateMachine.WordExplanation.State.Mistake)
                                    }
                                }
                            },
                        )
                    )
                is Configuration.RoundEditing ->
                    GameScreenComponent.Child.RoundEditing(
                        RealRoundEditingComponent(
                            onExitGame = onExitGame,
                            wordsToEdit = configuration.wordsToEdit,
                            onGuessed = { index ->
                                coroutineScope.launch {
                                    structuralMutex.withLock {
                                        gameStateMachine.updateWordsExplanationResults(
                                            configuration.wordsToEdit.value.toKoneMutableList().apply {
                                                this[index] = GameStateMachine.WordExplanation(this[index].word, GameStateMachine.WordExplanation.State.Explained)
                                            }
                                        )
                                    }
                                }
                            },
                            onNotGuessed = { index ->
                                coroutineScope.launch {
                                    structuralMutex.withLock {
                                        gameStateMachine.updateWordsExplanationResults(
                                            configuration.wordsToEdit.value.toKoneMutableList().apply {
                                                this[index] = GameStateMachine.WordExplanation(this[index].word, GameStateMachine.WordExplanation.State.NotExplained)
                                            }
                                        )
                                    }
                                }
                            },
                            onMistake = { index ->
                                coroutineScope.launch {
                                    structuralMutex.withLock {
                                        gameStateMachine.updateWordsExplanationResults(
                                            configuration.wordsToEdit.value.toKoneMutableList().apply {
                                                this[index] = GameStateMachine.WordExplanation(this[index].word, GameStateMachine.WordExplanation.State.Mistake)
                                            }
                                        )
                                    }
                                }
                            },
                            onConfirm = {
                                coroutineScope.launch {
                                    structuralMutex.withLock {
                                        gameStateMachine.confirmWordsExplanationResults()
                                    }
                                }
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
    
    init {
        coroutineScope.launch {
            gameStateMachine.state.collect { newState ->
                runOnUiThread {
                    navigation.updateCurrent { currentConfiguration ->
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
            }
        }
    }
    
    sealed interface Configuration {
        data object Loading : Configuration
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