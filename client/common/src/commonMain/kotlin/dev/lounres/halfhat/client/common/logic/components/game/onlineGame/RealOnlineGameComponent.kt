package dev.lounres.halfhat.client.common.logic.components.game.onlineGame

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.common.utils.defaultHttpClient
import dev.lounres.halfhat.client.components.LogicComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import io.ktor.client.plugins.websocket.converter
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.serialization.deserialize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.io.IOException


public class RealOnlineGameComponent(
    componentContext: LogicComponentContext,
) : OnlineGameComponent {
    override val connectionStatus: MutableStateFlow<ConnectionStatus> = MutableStateFlow(ConnectionStatus.Disconnected)
    override val freeRoomIdFlow: MutableSharedFlow<String> = MutableSharedFlow(extraBufferCapacity = 1)
    override val roomDescriptionFlow: MutableSharedFlow<ServerApi.RoomDescription> = MutableSharedFlow(extraBufferCapacity = 1)
    override val gameStateFlow: MutableStateFlow<ServerApi.OnlineGame.State?> = MutableStateFlow(null)
    
    private val outgoingSignals = Channel<ClientApi.Signal>(Channel.UNLIMITED)
    
    override fun sendSignal(signal: ClientApi.Signal) { outgoingSignals.trySend(signal) }
    
    override fun resetGameState() {
        gameStateFlow.value = null
    }
    
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
//                                logger.info(
//                                    items = {
//                                        mapOf(
//                                            "frame" to frame.toString(),
//                                        )
//                                    }
//                                ) { "Received inconvertible frame" }
                                continue
                            }
                            
                            when (val signal = converter.deserialize<ServerApi.Signal>(frame)) {
                                is ServerApi.Signal.RoomInfo -> roomDescriptionFlow.emit(signal.info)
                                is ServerApi.Signal.OnlineGameStateUpdate -> gameStateFlow.value = signal.state
                                is ServerApi.Signal.OnlineGameError -> {} // TODO
                            }
                        }
                        cancel()
                    }
                } catch (exception: IOException) {
//                    logger.warn(throwable = exception) { "Online game websocket connection exception" }
                }
                connectionStatus.value = ConnectionStatus.Disconnected
                gameStateFlow.value = null
                delay(1000)
            }
        }
    }
}