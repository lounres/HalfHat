package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.common.utils.runOnUiThread
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultStack
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.gameResults.RealGameResultsComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.loading.RealLoadingComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomScreen.RealRoomScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomSettings.RealRoomSettingsComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundEditing.RealRoundEditingComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundExplanation.RealRoundExplanationComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundLastGuess.RealRoundLastGuessComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundPreparation.RealRoundPreparationComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundWaiting.RealRoundWaitingComponent
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.komponentual.navigation.MutableStackNavigation
import dev.lounres.komponentual.navigation.replaceCurrent
import dev.lounres.komponentual.navigation.updateCurrent
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.state.KoneState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class RealGameScreenComponent(
    componentContext: UIComponentContext,
    gameStateFlow: StateFlow<ServerApi.OnlineGame.State?>,
    override val onExitOnlineGame: () -> Unit,
    onApplySettings: (ClientApi.SettingsBuilder) -> Unit,
    onStartGame: () -> Unit,
    onFinishGame: () -> Unit,
    onSpeakerReady: () -> Unit,
    onListenerReady: () -> Unit,
    onExplanationResult: (GameStateMachine.WordExplanation.State) -> Unit,
    onUpdateExplanationResults: (KoneList<GameStateMachine.WordExplanation>) -> Unit,
    onConfirmExplanationResults: () -> Unit,
) : GameScreenComponent {
    
    override val onCopyOnlineGameKey: () -> Unit = { TODO() }
    override val onCopyOnlineGameLink: () -> Unit = { TODO() }

    private val navigation = MutableStackNavigation<Configuration>()

    override val childStack: KoneState<ChildrenStack<Configuration, GameScreenComponent.Child>> =
        componentContext.uiChildrenDefaultStack(
            source = navigation,
            initialStack = {
                KoneList.of(
                    when(val gameState = gameStateFlow.value) {
                        null -> Configuration.Loading
                        is ServerApi.OnlineGame.State.GameInitialisation -> Configuration.RoomScreen(MutableStateFlow(gameState))
                        is ServerApi.OnlineGame.State.RoundWaiting -> Configuration.RoundWaiting(MutableStateFlow(gameState))
                        is ServerApi.OnlineGame.State.RoundPreparation -> Configuration.RoundPreparation(MutableStateFlow(gameState))
                        is ServerApi.OnlineGame.State.RoundExplanation -> Configuration.RoundExplanation(MutableStateFlow(gameState))
                        is ServerApi.OnlineGame.State.RoundLastGuess -> Configuration.RoundLastGuess(MutableStateFlow(gameState))
                        is ServerApi.OnlineGame.State.RoundEditing -> Configuration.RoundEditing(MutableStateFlow(gameState))
                        is ServerApi.OnlineGame.State.GameResults -> Configuration.GameResults(MutableStateFlow(gameState))
                    }
                )
            },
        ) { configuration, _ ->
            when(configuration) {
                Configuration.Loading ->
                    GameScreenComponent.Child.Loading(
                        RealLoadingComponent(
                        )
                    )
                is Configuration.RoomScreen ->
                    GameScreenComponent.Child.RoomScreen(
                        RealRoomScreenComponent(
                            gameStateFlow = configuration.stateFlow,
                            
                            onOpenGameSettings = { navigation.replaceCurrent(Configuration.RoomSettings(configuration.stateFlow)) },
                            onStartGame = onStartGame
                        )
                    )
                is Configuration.RoomSettings ->
                    GameScreenComponent.Child.RoomSettings(
                        RealRoomSettingsComponent(
                            onApplySettings = {
                                onApplySettings(it)
                                navigation.replaceCurrent(Configuration.RoomScreen(configuration.stateFlow))
                            },
                            onDiscardSettings = {
                                navigation.replaceCurrent(Configuration.RoomScreen(configuration.stateFlow))
                            },
                            
                            initialPreparationTimeSeconds = configuration.stateFlow.value.settingsBuilder.preparationTimeSeconds,
                            initialExplanationTimeSeconds = configuration.stateFlow.value.settingsBuilder.explanationTimeSeconds,
                            initialFinalGuessTimeSeconds = configuration.stateFlow.value.settingsBuilder.finalGuessTimeSeconds,
                            initialStrictMode = configuration.stateFlow.value.settingsBuilder.strictMode,
                            initialCachedEndConditionWordsNumber = configuration.stateFlow.value.settingsBuilder.cachedEndConditionWordsNumber,
                            initialCachedEndConditionCyclesNumber = configuration.stateFlow.value.settingsBuilder.cachedEndConditionCyclesNumber,
                            initialGameEndConditionType = configuration.stateFlow.value.settingsBuilder.gameEndConditionType,
                        )
                    )
                is Configuration.RoundWaiting ->
                    GameScreenComponent.Child.RoundWaiting(
                        RealRoundWaitingComponent(
                            onFinishGame = onFinishGame,
                            
                            gameState = configuration.stateFlow,
                            
                            onSpeakerReady = onSpeakerReady,
                            onListenerReady = onListenerReady,
                        )
                    )
                is Configuration.RoundPreparation ->
                    GameScreenComponent.Child.RoundPreparation(
                        RealRoundPreparationComponent(
                            gameState = configuration.stateFlow,
                        )
                    )
                is Configuration.RoundExplanation ->
                    GameScreenComponent.Child.RoundExplanation(
                        RealRoundExplanationComponent(
                            gameState = configuration.stateFlow,
                            
                            onExplanationResult = onExplanationResult,
                        )
                    )
                is Configuration.RoundLastGuess ->
                    GameScreenComponent.Child.RoundLastGuess(
                        RealRoundLastGuessComponent(
                            gameState = configuration.stateFlow,
                            
                            onExplanationResult = onExplanationResult,
                        )
                    )
                is Configuration.RoundEditing ->
                    GameScreenComponent.Child.RoundEditing(
                        RealRoundEditingComponent(
                            gameState = configuration.stateFlow,
                            
                            onUpdateExplanationResults = onUpdateExplanationResults,
                            
                            onConfirm = onConfirmExplanationResults,
                        )
                    )
                is Configuration.GameResults ->
                    GameScreenComponent.Child.GameResults(
                        RealGameResultsComponent(
                            gameState = configuration.stateFlow,
                        )
                    )
            }
        }

    init {
        componentContext.coroutineScope(Dispatchers.Default).launch {
            gameStateFlow.collect { newState ->
                runOnUiThread {
                    navigation.updateCurrent { currentConfiguration ->
                        when (newState) {
                            null -> Configuration.Loading
                            is ServerApi.OnlineGame.State.GameInitialisation ->
                                when (currentConfiguration) {
                                    is Configuration.RoomScreen ->
                                        currentConfiguration.apply {
                                            stateFlow.value = newState
                                        }
                                    is Configuration.RoomSettings ->
                                        currentConfiguration.apply {
                                            stateFlow.value = newState
                                        }
                                    else -> Configuration.RoomScreen(
                                        stateFlow = MutableStateFlow(newState),
                                    )
                                }
                            is ServerApi.OnlineGame.State.RoundWaiting ->
                                when (currentConfiguration) {
                                    is Configuration.RoundWaiting ->
                                        currentConfiguration.apply {
                                            stateFlow.value = newState
                                        }
                                    else -> Configuration.RoundWaiting(
                                        stateFlow = MutableStateFlow(newState),
                                    )
                                }
                            is ServerApi.OnlineGame.State.RoundPreparation ->
                                when (currentConfiguration) {
                                    is Configuration.RoundPreparation ->
                                        currentConfiguration.apply {
                                            stateFlow.value = newState
                                        }
                                    else -> Configuration.RoundPreparation(
                                        stateFlow = MutableStateFlow(newState),
                                    )
                                }
                            is ServerApi.OnlineGame.State.RoundExplanation ->
                                when (currentConfiguration) {
                                    is Configuration.RoundExplanation ->
                                        currentConfiguration.apply {
                                            stateFlow.value = newState
                                        }
                                    else -> Configuration.RoundExplanation(
                                        stateFlow = MutableStateFlow(newState),
                                    )
                                }
                            is ServerApi.OnlineGame.State.RoundLastGuess ->
                                when (currentConfiguration) {
                                    is Configuration.RoundLastGuess ->
                                        currentConfiguration.apply {
                                            stateFlow.value = newState
                                        }
                                    else -> Configuration.RoundLastGuess(
                                        stateFlow = MutableStateFlow(newState),
                                    )
                                }
                            is ServerApi.OnlineGame.State.RoundEditing ->
                                when (currentConfiguration) {
                                    is Configuration.RoundEditing ->
                                        currentConfiguration.apply {
                                            stateFlow.value = newState
                                        }
                                    else -> Configuration.RoundEditing(
                                        stateFlow = MutableStateFlow(newState),
                                    )
                                }
                            is ServerApi.OnlineGame.State.GameResults ->
                                when (currentConfiguration) {
                                    is Configuration.GameResults ->
                                        currentConfiguration.apply {
                                            stateFlow.value = newState
                                        }
                                    else -> Configuration.GameResults(
                                        stateFlow = MutableStateFlow(newState),
                                    )
                                }
                        }
                    }
                }
            }
        }
    }

    sealed interface Configuration {
        data object Loading : Configuration
        data class RoomScreen(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.GameInitialisation>,
        ) : Configuration
        data class RoomSettings(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.GameInitialisation>,
        ) : Configuration
        data class RoundWaiting(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.RoundWaiting>,
        ) : Configuration
        data class RoundPreparation(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.RoundPreparation>,
        ) : Configuration
        data class RoundExplanation(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.RoundExplanation>,
        ) : Configuration
        data class RoundLastGuess(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.RoundLastGuess>,
        ) : Configuration
        data class RoundEditing(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.RoundEditing>,
        ) : Configuration
        data class GameResults(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.GameResults>,
        ) : Configuration
    }
}