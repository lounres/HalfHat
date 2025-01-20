package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.lounres.halfhat.api.client.ClientApi
import dev.lounres.halfhat.api.server.BetterBeReplaced
import dev.lounres.halfhat.api.server.ServerApi
import dev.lounres.halfhat.client.common.logger
import dev.lounres.halfhat.client.common.utils.defaultHttpClient
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.FakeGameScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.RealGameScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.previewScreen.RealPreviewScreenComponent
import dev.lounres.logKube.core.info
import dev.lounres.logKube.core.warn
import io.ktor.client.plugins.websocket.converter
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.serialization.deserialize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.io.IOException


class RealOnlineGamePageComponent(
    componentContext: ComponentContext,
    onExitOnlineGame: () -> Unit
) : OnlineGamePageComponent {
    
    private val outgoingSignals = Channel<ClientApi.Signal>(Channel.UNLIMITED)
    
    private val freeRoomIdFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val roomDescriptionFlow = MutableSharedFlow<ServerApi.RoomDescription>(extraBufferCapacity = 1)
    private val gameStateFlow = MutableStateFlow<ServerApi.OnlineGame.State?>(null)
    
    init {
        with(componentContext.coroutineScope(Dispatchers.Default)) {
            launch {
                while (true) {
                    try {
                        defaultHttpClient.webSocket(host = "localhost", port = 3000, path = "/ws") {
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
                        }
                    } catch (exception: IOException) {
                        logger.warn(throwable = exception) { "Online game websocket connection exception" }
                    }
                    delay(1000)
                }
            }
        }
    }
    
    private val navigation = StackNavigation<Configuration>()
    
    override val childStack: Value<ChildStack<Configuration, OnlineGamePageComponent.Child>> =
        componentContext.childStack(
            source = navigation,
            serializer = null,
            initialConfiguration = Configuration.PreviewScreen,
        ) { configuration: Configuration, componentContext: ComponentContext ->
            when (configuration) {
                Configuration.PreviewScreen ->
                    OnlineGamePageComponent.Child.PreviewScreen(
                        RealPreviewScreenComponent(
                            componentContext = componentContext,
                            onExitOnlineGame = onExitOnlineGame,
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
                                navigation.replaceCurrent(Configuration.GameScreen)
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