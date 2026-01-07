package dev.lounres.halfhat.client.logic.components.game.onlineGame

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.utils.defaultHttpClient
import dev.lounres.halfhat.client.components.LogicComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.consts.OnlineGameSettings
import dev.lounres.halfhat.client.logic.settings.playExplanationStart
import dev.lounres.halfhat.client.logic.settings.playFinalGuessEnd
import dev.lounres.halfhat.client.logic.settings.playFinalGuessStart
import dev.lounres.halfhat.client.logic.settings.playPreparationCountdown
import dev.lounres.halfhat.client.logic.settings.volumeOn
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.halfhat.client.utils.logger
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
import kotlinx.coroutines.flow.getAndUpdate
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
        val settings = componentContext.settings
        val volumeOn = settings.volumeOn
        
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
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessEnd() }
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
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessEnd() }
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
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundEditing -> {}
                                                is ServerApi.OnlineGame.State.GameResults -> {}
                                            }
                                        is ServerApi.OnlineGame.State.RoundPreparation ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation ->
                                                    launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection ->
                                                    launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.RoundWaiting ->
                                                    launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.RoundPreparation ->
                                                    if (previousState.roundNumber != newState.roundNumber || (previousState.millisecondsLeft / 1000u) != (newState.millisecondsLeft / 1000u))
                                                        launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.RoundEditing ->
                                                    launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.GameResults ->
                                                    launch { settings.playPreparationCountdown() }
                                            }
                                        is ServerApi.OnlineGame.State.RoundExplanation ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation ->
                                                    launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection ->
                                                    launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.RoundWaiting ->
                                                    launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.RoundPreparation ->
                                                    launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    if (previousState.roundNumber != newState.roundNumber)
                                                        launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.RoundEditing ->
                                                    launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.GameResults ->
                                                    launch { settings.playExplanationStart() }
                                            }
                                        is ServerApi.OnlineGame.State.RoundLastGuess ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessStart() }
                                                    else
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessStart() }
                                                    else
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundWaiting ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessStart() }
                                                    else
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundPreparation ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessStart() }
                                                    else
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessStart() }
                                                    else
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    if (newState.millisecondsLeft == 0u) {
                                                        if (newState.roundNumber != previousState.roundNumber)
                                                            launch { settings.playFinalGuessEnd() }
                                                    } else {
                                                        if (newState.roundNumber != previousState.roundNumber)
                                                            launch { settings.playFinalGuessStart() }
                                                    }
                                                is ServerApi.OnlineGame.State.RoundEditing ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessStart() }
                                                    else
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.GameResults ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessStart() }
                                                    else
                                                        launch { settings.playFinalGuessEnd() }
                                            }
                                        is ServerApi.OnlineGame.State.RoundEditing ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation -> {}
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection -> {}
                                                is ServerApi.OnlineGame.State.RoundWaiting -> {}
                                                is ServerApi.OnlineGame.State.RoundPreparation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessEnd() }
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
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundExplanation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.RoundLastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessEnd() }
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