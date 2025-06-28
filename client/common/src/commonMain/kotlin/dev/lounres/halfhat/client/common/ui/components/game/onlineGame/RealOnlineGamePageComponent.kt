package dev.lounres.halfhat.client.common.ui.components.game.onlineGame

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.client.common.logic.components.game.onlineGame.ConnectionStatus
import dev.lounres.halfhat.client.common.logic.components.game.onlineGame.OnlineGameComponent
import dev.lounres.halfhat.client.common.logic.components.game.onlineGame.RealOnlineGameComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.RealGameScreenComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.previewScreen.RealPreviewScreenComponent
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.logicChildOnRunning
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultStack
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.komponentual.navigation.MutableStackNavigation
import dev.lounres.komponentual.navigation.replaceCurrent
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.state.KoneAsynchronousState
import dev.lounres.kone.state.KoneState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


public class RealOnlineGamePageComponent(
    override val onExitOnlineGameMode: () -> Unit,
    private val onlineGameComponent: OnlineGameComponent,
    override val childStack: KoneAsynchronousState<ChildrenStack<*, OnlineGamePageComponent.Child>>,
) : OnlineGamePageComponent {
    override val connectionStatus: StateFlow<ConnectionStatus> get() = onlineGameComponent.connectionStatus
    
    public sealed interface Configuration {
        public data object PreviewScreen : Configuration
        public data object GameScreen : Configuration
    }
}

public suspend fun RealOnlineGamePageComponent(
    componentContext: UIComponentContext,
    onExitOnlineGameMode: () -> Unit,
): RealOnlineGamePageComponent {
    val onlineGameComponent: OnlineGameComponent = RealOnlineGameComponent(componentContext.logicChildOnRunning())
    
    val navigation = MutableStackNavigation<RealOnlineGamePageComponent.Configuration>(CoroutineScope(Dispatchers.Default))
    
    val childStack: KoneAsynchronousState<ChildrenStack<RealOnlineGamePageComponent.Configuration, OnlineGamePageComponent.Child>> =
        componentContext.uiChildrenDefaultStack(
            source = navigation,
            initialStack = KoneList.of(RealOnlineGamePageComponent.Configuration.PreviewScreen),
        ) { configuration: RealOnlineGamePageComponent.Configuration, componentContext: UIComponentContext ->
            when (configuration) {
                RealOnlineGamePageComponent.Configuration.PreviewScreen ->
                    OnlineGamePageComponent.Child.PreviewScreen(
                        RealPreviewScreenComponent(
                            componentContext = componentContext,
                            onFetchFreeRoomId = { onlineGameComponent.sendSignal(ClientApi.Signal.FetchFreeRoomId) },
                            freeRoomIdFlow = onlineGameComponent.freeRoomIdFlow,
                            onFetchRoomInfo = { onlineGameComponent.sendSignal(ClientApi.Signal.FetchRoomInfo(it)) },
                            roomDescriptionFlow = onlineGameComponent.roomDescriptionFlow,
                            onEnterRoom = { roomId, playerName ->
                                onlineGameComponent.resetGameState()
                                onlineGameComponent.sendSignal(
                                    ClientApi.Signal.OnlineGame.JoinRoom(
                                        roomId = roomId,
                                        playerName = playerName
                                    )
                                )
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.replaceCurrent(RealOnlineGamePageComponent.Configuration.GameScreen)
                                }
                            }
                        )
                    )
                RealOnlineGamePageComponent.Configuration.GameScreen ->
                    OnlineGamePageComponent.Child.GameScreen(
                        RealGameScreenComponent(
                            componentContext = componentContext,
                            gameStateFlow = onlineGameComponent.gameStateFlow,
                            onExitOnlineGame = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.LeaveRoom)
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.replaceCurrent(RealOnlineGamePageComponent.Configuration.PreviewScreen)
                                }
                            },
                            onApplySettings = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.UpdateSettings(it))
                            },
                            onStartGame = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.InitializeGame)
                            },
                            onFinishGame = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.FinishGame)
                            },
                            onSpeakerReady = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.SpeakerReady)
                            },
                            onListenerReady = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.ListenerReady)
                            },
                            onExplanationResult = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.WordExplanationState(it))
                            },
                            onUpdateExplanationResults = {
                                onlineGameComponent.sendSignal(
                                    ClientApi.Signal.OnlineGame.UpdateWordsExplanationResults(
                                        it
                                    )
                                )
                            },
                            onConfirmExplanationResults = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.ConfirmWordsExplanationResults)
                            }
                        )
                    )
            }
        }
    
    return RealOnlineGamePageComponent(
        onExitOnlineGameMode = onExitOnlineGameMode,
        onlineGameComponent = onlineGameComponent,
        childStack = childStack,
    )
}