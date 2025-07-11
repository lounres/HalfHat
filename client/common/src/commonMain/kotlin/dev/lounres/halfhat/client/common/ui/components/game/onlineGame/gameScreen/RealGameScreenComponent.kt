package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.gameResults.RealGameResultsComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.loading.RealLoadingComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roomScreen.RealRoomScreenComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roomSettings.RealRoomSettingsComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundEditing.RealRoundEditingComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundExplanation.RealRoundExplanationComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundLastGuess.RealRoundLastGuessComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundPreparation.RealRoundPreparationComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundWaiting.RealRoundWaitingComponent
import dev.lounres.halfhat.client.common.utils.runOnUiThread
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultStack
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.komponentual.navigation.MutableStackNavigation
import dev.lounres.komponentual.navigation.replaceCurrent
import dev.lounres.komponentual.navigation.updateCurrent
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.state.KoneAsynchronousState
import dev.lounres.kone.state.KoneState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


public class RealGameScreenComponent(
    override val onExitOnlineGame: () -> Unit,
    override val childStack: KoneAsynchronousState<ChildrenStack<*, GameScreenComponent.Child>>,
) : GameScreenComponent {
    
    override val onCopyOnlineGameKey: () -> Unit = { TODO() }
    override val onCopyOnlineGameLink: () -> Unit = { TODO() }

    public sealed interface Configuration {
        public data object Loading : Configuration
        public data class RoomScreen(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.GameInitialisation>,
        ) : Configuration
        public data class RoomSettings(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.GameInitialisation>,
        ) : Configuration
        public data class RoundWaiting(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.RoundWaiting>,
        ) : Configuration
        public data class RoundPreparation(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.RoundPreparation>,
        ) : Configuration
        public data class RoundExplanation(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.RoundExplanation>,
        ) : Configuration
        public data class RoundLastGuess(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.RoundLastGuess>,
        ) : Configuration
        public data class RoundEditing(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.RoundEditing>,
        ) : Configuration
        public data class GameResults(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.GameResults>,
        ) : Configuration
    }
}

public suspend fun RealGameScreenComponent(
    componentContext: UIComponentContext,
    gameStateFlow: StateFlow<ServerApi.OnlineGame.State?>,
    onExitOnlineGame: () -> Unit,
    onApplySettings: (ClientApi.SettingsBuilder) -> Unit,
    onStartGame: () -> Unit,
    onFinishGame: () -> Unit,
    onSpeakerReady: () -> Unit,
    onListenerReady: () -> Unit,
    onExplanationResult: (GameStateMachine.WordExplanation.State) -> Unit,
    onUpdateExplanationResults: (KoneList<GameStateMachine.WordExplanation>) -> Unit,
    onConfirmExplanationResults: () -> Unit,
): RealGameScreenComponent {
    val navigation = MutableStackNavigation<RealGameScreenComponent.Configuration>()
    
    val childStack: KoneAsynchronousState<ChildrenStack<RealGameScreenComponent.Configuration, GameScreenComponent.Child>> =
        componentContext.uiChildrenDefaultStack(
            source = navigation,
            initialStack = KoneList.of(
                when(val gameState = gameStateFlow.value) {
                    null -> RealGameScreenComponent.Configuration.Loading
                    is ServerApi.OnlineGame.State.GameInitialisation -> RealGameScreenComponent.Configuration.RoomScreen(MutableStateFlow(gameState))
                    is ServerApi.OnlineGame.State.RoundWaiting -> RealGameScreenComponent.Configuration.RoundWaiting(MutableStateFlow(gameState))
                    is ServerApi.OnlineGame.State.RoundPreparation -> RealGameScreenComponent.Configuration.RoundPreparation(MutableStateFlow(gameState))
                    is ServerApi.OnlineGame.State.RoundExplanation -> RealGameScreenComponent.Configuration.RoundExplanation(MutableStateFlow(gameState))
                    is ServerApi.OnlineGame.State.RoundLastGuess -> RealGameScreenComponent.Configuration.RoundLastGuess(MutableStateFlow(gameState))
                    is ServerApi.OnlineGame.State.RoundEditing -> RealGameScreenComponent.Configuration.RoundEditing(MutableStateFlow(gameState))
                    is ServerApi.OnlineGame.State.GameResults -> RealGameScreenComponent.Configuration.GameResults(MutableStateFlow(gameState))
                }
            ),
        ) { configuration, _ ->
            when(configuration) {
                RealGameScreenComponent.Configuration.Loading ->
                    GameScreenComponent.Child.Loading(
                        RealLoadingComponent(
                        )
                    )
                is RealGameScreenComponent.Configuration.RoomScreen ->
                    GameScreenComponent.Child.RoomScreen(
                        RealRoomScreenComponent(
                            gameStateFlow = configuration.stateFlow,
                            
                            onOpenGameSettings = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.replaceCurrent(
                                        RealGameScreenComponent.Configuration.RoomSettings(
                                            configuration.stateFlow
                                        )
                                    )
                                }
                            },
                            onStartGame = onStartGame
                        )
                    )
                is RealGameScreenComponent.Configuration.RoomSettings ->
                    GameScreenComponent.Child.RoomSettings(
                        RealRoomSettingsComponent(
                            onApplySettings = {
                                onApplySettings(it)
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.replaceCurrent(
                                        RealGameScreenComponent.Configuration.RoomScreen(
                                            configuration.stateFlow
                                        )
                                    )
                                }
                            },
                            onDiscardSettings = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.replaceCurrent(
                                        RealGameScreenComponent.Configuration.RoomScreen(
                                            configuration.stateFlow
                                        )
                                    )
                                }
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
                is RealGameScreenComponent.Configuration.RoundWaiting ->
                    GameScreenComponent.Child.RoundWaiting(
                        RealRoundWaitingComponent(
                            onFinishGame = onFinishGame,
                            
                            gameState = configuration.stateFlow,
                            
                            onSpeakerReady = onSpeakerReady,
                            onListenerReady = onListenerReady,
                        )
                    )
                is RealGameScreenComponent.Configuration.RoundPreparation ->
                    GameScreenComponent.Child.RoundPreparation(
                        RealRoundPreparationComponent(
                            gameState = configuration.stateFlow,
                        )
                    )
                is RealGameScreenComponent.Configuration.RoundExplanation ->
                    GameScreenComponent.Child.RoundExplanation(
                        RealRoundExplanationComponent(
                            gameState = configuration.stateFlow,
                            
                            onExplanationResult = onExplanationResult,
                        )
                    )
                is RealGameScreenComponent.Configuration.RoundLastGuess ->
                    GameScreenComponent.Child.RoundLastGuess(
                        RealRoundLastGuessComponent(
                            gameState = configuration.stateFlow,
                            
                            onExplanationResult = onExplanationResult,
                        )
                    )
                is RealGameScreenComponent.Configuration.RoundEditing ->
                    GameScreenComponent.Child.RoundEditing(
                        RealRoundEditingComponent(
                            gameState = configuration.stateFlow,
                            
                            onUpdateExplanationResults = onUpdateExplanationResults,
                            
                            onConfirm = onConfirmExplanationResults,
                        )
                    )
                is RealGameScreenComponent.Configuration.GameResults ->
                    GameScreenComponent.Child.GameResults(
                        RealGameResultsComponent(
                            gameState = configuration.stateFlow,
                        )
                    )
            }
        }
    
    componentContext.coroutineScope(Dispatchers.Default).launch {
        gameStateFlow.collect { newState ->
            navigation.updateCurrent { currentConfiguration ->
                when (newState) {
                    null -> RealGameScreenComponent.Configuration.Loading
                    is ServerApi.OnlineGame.State.GameInitialisation ->
                        when (currentConfiguration) {
                            is RealGameScreenComponent.Configuration.RoomScreen ->
                                currentConfiguration.apply {
                                    stateFlow.value = newState
                                }
                            is RealGameScreenComponent.Configuration.RoomSettings ->
                                currentConfiguration.apply {
                                    stateFlow.value = newState
                                }
                            else -> RealGameScreenComponent.Configuration.RoomScreen(
                                stateFlow = MutableStateFlow(newState),
                            )
                        }
                    is ServerApi.OnlineGame.State.RoundWaiting ->
                        when (currentConfiguration) {
                            is RealGameScreenComponent.Configuration.RoundWaiting ->
                                currentConfiguration.apply {
                                    stateFlow.value = newState
                                }
                            else -> RealGameScreenComponent.Configuration.RoundWaiting(
                                stateFlow = MutableStateFlow(newState),
                            )
                        }
                    is ServerApi.OnlineGame.State.RoundPreparation ->
                        when (currentConfiguration) {
                            is RealGameScreenComponent.Configuration.RoundPreparation ->
                                currentConfiguration.apply {
                                    stateFlow.value = newState
                                }
                            else -> RealGameScreenComponent.Configuration.RoundPreparation(
                                stateFlow = MutableStateFlow(newState),
                            )
                        }
                    is ServerApi.OnlineGame.State.RoundExplanation ->
                        when (currentConfiguration) {
                            is RealGameScreenComponent.Configuration.RoundExplanation ->
                                currentConfiguration.apply {
                                    stateFlow.value = newState
                                }
                            else -> RealGameScreenComponent.Configuration.RoundExplanation(
                                stateFlow = MutableStateFlow(newState),
                            )
                        }
                    is ServerApi.OnlineGame.State.RoundLastGuess ->
                        when (currentConfiguration) {
                            is RealGameScreenComponent.Configuration.RoundLastGuess ->
                                currentConfiguration.apply {
                                    stateFlow.value = newState
                                }
                            else -> RealGameScreenComponent.Configuration.RoundLastGuess(
                                stateFlow = MutableStateFlow(newState),
                            )
                        }
                    is ServerApi.OnlineGame.State.RoundEditing ->
                        when (currentConfiguration) {
                            is RealGameScreenComponent.Configuration.RoundEditing ->
                                currentConfiguration.apply {
                                    stateFlow.value = newState
                                }
                            else -> RealGameScreenComponent.Configuration.RoundEditing(
                                stateFlow = MutableStateFlow(newState),
                            )
                        }
                    is ServerApi.OnlineGame.State.GameResults ->
                        when (currentConfiguration) {
                            is RealGameScreenComponent.Configuration.GameResults ->
                                currentConfiguration.apply {
                                    stateFlow.value = newState
                                }
                            else -> RealGameScreenComponent.Configuration.GameResults(
                                stateFlow = MutableStateFlow(newState),
                            )
                        }
                }
            }
        }
    }
    
    return RealGameScreenComponent(
        onExitOnlineGame = onExitOnlineGame,
        childStack = childStack,
    )
}