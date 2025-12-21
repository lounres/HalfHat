package dev.lounres.halfhat.client.logic.components.game.onlineGame

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.utils.defaultHttpClient
import dev.lounres.halfhat.client.components.LogicComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.consts.OnlineGameSettings
import dev.lounres.halfhat.client.utils.DefaultSounds
import dev.lounres.halfhat.client.utils.logger
import dev.lounres.halfhat.client.utils.play
import dev.lounres.logKube.core.debug
import dev.lounres.logKube.core.info
import dev.lounres.logKube.core.warn
import io.ktor.client.plugins.websocket.converter
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.URLProtocol
import io.ktor.serialization.deserialize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.io.IOException


public class RealOnlineGameComponent(
    componentContext: LogicComponentContext,
    volumeOn: StateFlow<Boolean>,
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
                    defaultHttpClient.webSocket(
                        host = OnlineGameSettings.host,
                        port = OnlineGameSettings.port,
                        path = OnlineGameSettings.path,
                        request = {
                            url.protocol =
                                if (OnlineGameSettings.isSecure) URLProtocol.WSS
                                else URLProtocol.WS
                        }
                    ) {
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
                                ) { "Received inconvertible frame. Skipping." }
                                continue
                            }
                            
                            val signal = converter.deserialize<ServerApi.Signal>(frame)
                            
                            logger.debug(
                                items = {
                                    mapOf(
                                        "signal" to signal.toString(),
                                    )
                                }
                            ) { "Received signal: $signal" }
                            
                            when (signal) {
                                is ServerApi.Signal.RoomInfo -> roomDescriptionFlow.emit(signal.info)
                                is ServerApi.Signal.OnlineGameStateUpdate -> {
                                    val newState = signal.state
                                    val previousState = gameStateFlow.getAndUpdate { newState }
                                    if (volumeOn.value) when (newState) {
                                        is ServerApi.OnlineGame.State.GameInitialisation ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation -> {}
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection -> {}
                                                is ServerApi.OnlineGame.State.RoundWaiting -> {}
                                                is ServerApi.OnlineGame.State.RoundPreparation ->
                                                    launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundEditing -> {}
                                                is ServerApi.OnlineGame.State.GameResults -> {}
                                            }
                                        is ServerApi.OnlineGame.State.PlayersWordsCollection ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation -> {}
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection -> {}
                                                is ServerApi.OnlineGame.State.RoundWaiting -> {}
                                                is ServerApi.OnlineGame.State.RoundPreparation ->
                                                    launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundEditing -> {}
                                                is ServerApi.OnlineGame.State.GameResults -> {}
                                            }
                                        is ServerApi.OnlineGame.State.RoundWaiting ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation -> {}
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection -> {}
                                                is ServerApi.OnlineGame.State.RoundWaiting -> {}
                                                is ServerApi.OnlineGame.State.RoundPreparation ->
                                                    launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundEditing -> {}
                                                is ServerApi.OnlineGame.State.GameResults -> {}
                                            }
                                        is ServerApi.OnlineGame.State.RoundPreparation ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation ->
                                                    launch { DefaultSounds.preparationCountdown.await().play() }
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection ->
                                                    launch { DefaultSounds.preparationCountdown.await().play() }
                                                is ServerApi.OnlineGame.State.RoundWaiting ->
                                                    launch { DefaultSounds.preparationCountdown.await().play() }
                                                is ServerApi.OnlineGame.State.RoundPreparation ->
                                                    if (previousState.roundNumber != newState.roundNumber || (previousState.millisecondsLeft / 1000u) != (newState.millisecondsLeft / 1000u))
                                                        launch { DefaultSounds.preparationCountdown.await().play() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    launch { DefaultSounds.preparationCountdown.await().play() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    launch { DefaultSounds.preparationCountdown.await().play() }
                                                is ServerApi.OnlineGame.State.RoundEditing ->
                                                    launch { DefaultSounds.preparationCountdown.await().play() }
                                                is ServerApi.OnlineGame.State.GameResults ->
                                                    launch { DefaultSounds.preparationCountdown.await().play() }
                                            }
                                        is ServerApi.OnlineGame.State.RoundExplanation ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation ->
                                                    launch { DefaultSounds.explanationStart.await().play() }
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection ->
                                                    launch { DefaultSounds.explanationStart.await().play() }
                                                is ServerApi.OnlineGame.State.RoundWaiting ->
                                                    launch { DefaultSounds.explanationStart.await().play() }
                                                is ServerApi.OnlineGame.State.RoundPreparation ->
                                                    launch { DefaultSounds.explanationStart.await().play() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    if (previousState.roundNumber != newState.roundNumber)
                                                        launch { DefaultSounds.explanationStart.await().play() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    launch { DefaultSounds.explanationStart.await().play() }
                                                is ServerApi.OnlineGame.State.RoundEditing ->
                                                    launch { DefaultSounds.explanationStart.await().play() }
                                                is ServerApi.OnlineGame.State.GameResults ->
                                                    launch { DefaultSounds.explanationStart.await().play() }
                                            }
                                        is ServerApi.OnlineGame.State.RoundLastGuess ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { DefaultSounds.finalGuessStart.await().play() }
                                                    else
                                                        launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { DefaultSounds.finalGuessStart.await().play() }
                                                    else
                                                        launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundWaiting ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { DefaultSounds.finalGuessStart.await().play() }
                                                    else
                                                        launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundPreparation ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { DefaultSounds.finalGuessStart.await().play() }
                                                    else
                                                        launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { DefaultSounds.finalGuessStart.await().play() }
                                                    else
                                                        launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    if (newState.millisecondsLeft == 0u) {
                                                        if (newState.roundNumber != previousState.roundNumber)
                                                            launch { DefaultSounds.finalGuessEnd.await().play() }
                                                    } else {
                                                        if (newState.roundNumber != previousState.roundNumber)
                                                            launch { DefaultSounds.finalGuessStart.await().play() }
                                                    }
                                                is ServerApi.OnlineGame.State.RoundEditing ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { DefaultSounds.finalGuessStart.await().play() }
                                                    else
                                                        launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.GameResults ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { DefaultSounds.finalGuessStart.await().play() }
                                                    else
                                                        launch { DefaultSounds.finalGuessEnd.await().play() }
                                            }
                                        is ServerApi.OnlineGame.State.RoundEditing ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation -> {}
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection -> {}
                                                is ServerApi.OnlineGame.State.RoundWaiting -> {}
                                                is ServerApi.OnlineGame.State.RoundPreparation ->
                                                    launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundEditing -> {}
                                                is ServerApi.OnlineGame.State.GameResults -> {}
                                            }
                                        is ServerApi.OnlineGame.State.GameResults ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation -> {}
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection -> {}
                                                is ServerApi.OnlineGame.State.RoundWaiting -> {}
                                                is ServerApi.OnlineGame.State.RoundPreparation ->
                                                    launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { DefaultSounds.finalGuessEnd.await().play() }
                                                is ServerApi.OnlineGame.State.RoundEditing -> {}
                                                is ServerApi.OnlineGame.State.GameResults -> {}
                                            }
                                    }
                                }
                                is ServerApi.Signal.OnlineGameError -> {} // TODO
                            }
                        }
                        cancel()
                    }
                } catch (exception: IOException) {
                    logger.warn(throwable = exception) { "Online game websocket connection exception" }
                }
                connectionStatus.value = ConnectionStatus.Disconnected
                gameStateFlow.value = null
                delay(1000)
            }
        }
    }
}