package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame

import dev.lounres.halfhat.api.client.ClientApi
import dev.lounres.halfhat.api.server.BetterBeReplaced
import dev.lounres.halfhat.api.server.ServerApi
import dev.lounres.halfhat.client.common.logger
import dev.lounres.halfhat.client.common.utils.defaultHttpClient
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultStack
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.RealGameScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.previewScreen.RealPreviewScreenComponent
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.komponentual.navigation.MutableStackNavigation
import dev.lounres.komponentual.navigation.replaceCurrent
import dev.lounres.kone.state.KoneState
import dev.lounres.logKube.core.info
import dev.lounres.logKube.core.warn
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.io.IOException


class RealOnlineGamePageComponent(
    componentContext: UIComponentContext,
    override val onExitOnlineGameMode: () -> Unit,
) : OnlineGamePageComponent {
    
    private val outgoingSignals = Channel<ClientApi.Signal>(Channel.UNLIMITED)
    
    override val connectionStatus: MutableStateFlow<ConnectionStatus> = MutableStateFlow(ConnectionStatus.Disconnected)
    private val freeRoomIdFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val roomDescriptionFlow = MutableSharedFlow<ServerApi.RoomDescription>(extraBufferCapacity = 1)
    private val gameStateFlow = MutableStateFlow<ServerApi.OnlineGame.State?>(null)
    
    init {
        componentContext.coroutineScope(Dispatchers.Default).launch {
            while (true) {
                try {
                    defaultHttpClient.webSocket(host = "localhost", port = 3000, path = "/ws") {
                        connectionStatus.value = ConnectionStatus.Connected
                        val converter = converter!!
                        launch {
                            for (signal in outgoingSignals) {
                                sendSerialized<ClientApi.Signal>(signal)
                            }
                        }
                        for (frame in incoming) {
                            if (!converter.isApplicable(frame)) {
                                logger.info(
                                    items = {
                                        mapOf(
                                            "frame" to frame.toString(),
                                        )
                                    }
                                ) { "Received inconvertible frame" }
                                continue
                            }
                            val signal = converter.deserialize<ServerApi.Signal>(frame)
                            
                            when (signal) {
                                @OptIn(BetterBeReplaced::class)
                                ServerApi.Signal.UnspecifiedError -> {} // TODO
                                is ServerApi.Signal.Error -> {} // TODO
                                is ServerApi.Signal.OnlineGameStateUpdate -> gameStateFlow.value = signal.state
                                is ServerApi.Signal.RoomInfo -> roomDescriptionFlow.emit(signal.info)
                            }
                        }
                        cancel()
                    }
                } catch (exception: IOException) {
                    logger.warn(throwable = exception) { "Online game websocket connection exception" }
                }
                connectionStatus.value = ConnectionStatus.Disconnected
                delay(1000)
            }
        }
    }
    
    private val navigation = MutableStackNavigation<Configuration>()
    
    override val childStack: KoneState<ChildrenStack<Configuration, OnlineGamePageComponent.Child>> =
        componentContext.uiChildrenDefaultStack(
            source = navigation,
            initialStack = { KoneList.of(Configuration.PreviewScreen) },
        ) { configuration: Configuration, componentContext: UIComponentContext ->
            when (configuration) {
                Configuration.PreviewScreen ->
                    OnlineGamePageComponent.Child.PreviewScreen(
                        RealPreviewScreenComponent(
                            componentContext = componentContext,
                            onFetchFreeRoomId = { outgoingSignals.trySend(ClientApi.Signal.FetchFreeRoomId) },
                            freeRoomIdFlow = freeRoomIdFlow,
                            onFetchRoomInfo = { outgoingSignals.trySend(ClientApi.Signal.FetchRoomInfo(it)) },
                            roomDescriptionFlow = roomDescriptionFlow,
                            onEnterRoom = { roomId, playerName ->
                                gameStateFlow.value = null
                                outgoingSignals.trySend(
                                    ClientApi.Signal.OnlineGame.JoinRoom(
                                        roomId = roomId,
                                        playerName = playerName
                                    )
                                )
                                navigation.replaceCurrent(Configuration.GameScreen)
                            }
                        )
                    )
                Configuration.GameScreen ->
                    OnlineGamePageComponent.Child.GameScreen(
                        RealGameScreenComponent(
                            componentContext = componentContext,
                            gameStateFlow = gameStateFlow,
                            onExitOnlineGame = {
                                outgoingSignals.trySend(ClientApi.Signal.OnlineGame.LeaveRoom)
                                navigation.replaceCurrent(Configuration.PreviewScreen)
                            },
                            onApplySettings = {
                                outgoingSignals.trySend(ClientApi.Signal.OnlineGame.UpdateSettings(it))
                            },
                            onStartGame = {
                                outgoingSignals.trySend(ClientApi.Signal.OnlineGame.InitializeGame)
                            },
                            onFinishGame = {
                                outgoingSignals.trySend(ClientApi.Signal.OnlineGame.FinishGame)
                            },
                            onSpeakerReady = {
                                outgoingSignals.trySend(ClientApi.Signal.OnlineGame.SpeakerReady)
                            },
                            onListenerReady = {
                                outgoingSignals.trySend(ClientApi.Signal.OnlineGame.ListenerReady)
                            },
                            onExplanationResult = {
                                outgoingSignals.trySend(ClientApi.Signal.OnlineGame.WordExplanationState(it))
                            },
                            onUpdateExplanationResults = {
                                outgoingSignals.trySend(ClientApi.Signal.OnlineGame.UpdateWordsExplanationResults(it))
                            },
                            onConfirmExplanationResults = {
                                outgoingSignals.trySend(ClientApi.Signal.OnlineGame.ConfirmWordsExplanationResults)
                            }
                        )
                    )
            }
        }
    
    sealed interface Configuration {
        data object PreviewScreen : Configuration
        data object GameScreen : Configuration
    }
}