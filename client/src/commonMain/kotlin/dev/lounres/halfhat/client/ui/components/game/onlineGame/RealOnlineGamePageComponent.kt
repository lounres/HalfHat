package dev.lounres.halfhat.client.ui.components.game.onlineGame

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.client.logic.components.game.onlineGame.ConnectionStatus
import dev.lounres.halfhat.client.logic.components.game.onlineGame.OnlineGameComponent
import dev.lounres.halfhat.client.logic.components.game.onlineGame.RealOnlineGameComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.RealGameScreenComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.previewScreen.RealPreviewScreenComponent
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildLogicChildOnRunning
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.ChildrenStack
import dev.lounres.halfhat.client.components.navigation.controller.navigationContext
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultStackNode
import dev.lounres.halfhat.client.logic.settings.volumeOn
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.komponentual.navigation.replaceCurrent
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.hub.KoneAsynchronousHubView
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


public class RealOnlineGamePageComponent(
    override val onExitOnlineGameMode: () -> Unit,
    private val onlineGameComponent: OnlineGameComponent,
    override val childStack: KoneAsynchronousHubView<ChildrenStack<*, OnlineGamePageComponent.Child, UIComponentContext>, *>,
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
    val coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    val onlineGameComponent: OnlineGameComponent =
        componentContext.buildLogicChildOnRunning {
            RealOnlineGameComponent(it, componentContext.settings.volumeOn)
        }
    
    val currentRoomSearchEntry = KoneMutableAsynchronousHub("")
    coroutineScope.launch {
        onlineGameComponent.freeRoomIdFlow.collect {
            currentRoomSearchEntry.set(it)
        }
    }
    
    val childStack =
        componentContext.uiChildrenDefaultStackNode<RealOnlineGamePageComponent.Configuration, _>(
            initialStack = KoneList.of(RealOnlineGamePageComponent.Configuration.PreviewScreen),
        ) { configuration, componentContext, navigation ->
            when (configuration) {
                RealOnlineGamePageComponent.Configuration.PreviewScreen ->
                    OnlineGamePageComponent.Child.PreviewScreen(
                        RealPreviewScreenComponent(
                            componentContext = componentContext,
                            currentRoomSearchEntry = currentRoomSearchEntry,
                            onFetchFreeRoomId = { onlineGameComponent.sendSignal(ClientApi.Signal.FetchFreeRoomId) },
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
                                coroutineScope.launch {
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
                                coroutineScope.launch {
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
    
    componentContext.navigationContext
    
    return RealOnlineGamePageComponent(
        onExitOnlineGameMode = onExitOnlineGameMode,
        onlineGameComponent = onlineGameComponent,
        childStack = childStack.hub,
    )
}