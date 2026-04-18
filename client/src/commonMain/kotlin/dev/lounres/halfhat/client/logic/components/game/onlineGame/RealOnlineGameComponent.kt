package dev.lounres.halfhat.client.logic.components.game.onlineGame

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.api.onlineGame.DictionaryId
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.components.LogicComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.logger.logger
import dev.lounres.halfhat.client.consts.OnlineGameSettings
import dev.lounres.halfhat.client.logic.settings.*
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.halfhat.client.utils.defaultHttpClient
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.hub.value
import dev.lounres.logKube.core.debug
import dev.lounres.logKube.core.info
import dev.lounres.logKube.core.warn
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


public class RealOnlineGameComponent(
    componentContext: LogicComponentContext,
) : OnlineGameComponent {
    override val connectionStatus: MutableStateFlow<ConnectionStatus> = MutableStateFlow(ConnectionStatus.Disconnected)
    override val freeRoomIdFlow: MutableSharedFlow<String> = MutableSharedFlow(extraBufferCapacity = 1)
    override val roomDescriptionFlow: MutableSharedFlow<ServerApi.RoomDescription> = MutableSharedFlow(extraBufferCapacity = 1)
    override val gameStateFlow: MutableStateFlow<ServerApi.OnlineGame.State?> = MutableStateFlow(null)
    override val availableDictionariesFlow: MutableStateFlow<KoneList<DictionaryId.WithDescription>?> = MutableStateFlow(null)

    private val outgoingSignals = Channel<ClientApi.Signal>(Channel.UNLIMITED)
    
    override fun sendSignal(signal: ClientApi.Signal) { outgoingSignals.trySend(signal) }
    
    override fun resetGameState() {
        gameStateFlow.value = null
    }
    override fun resetAvailableDictionaries() {
        availableDictionariesFlow.value = null
    }
    
    init {
        val logger = componentContext.logger
        
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
                                logger.debug(
                                    items = {
                                        mapOf(
                                            "signal" to signal.toString(),
                                        )
                                    }
                                ) { "Sending signal" }
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
                            ) { "Received signal" }
                            
                            when (signal) {
                                is ServerApi.Signal.RoomInfo -> roomDescriptionFlow.emit(signal.info)
                                is ServerApi.Signal.AvailableDictionariesUpdate ->
                                    availableDictionariesFlow.value = signal.descriptions
                                is ServerApi.Signal.OnlineGameStateUpdate -> {
                                    val newState = signal.state
                                    val previousState = gameStateFlow.getAndUpdate { newState }
                                    if (volumeOn.value) when (newState) {
                                        is ServerApi.OnlineGame.State.RoomPlayersGathering ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.RoomPlayersGathering -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation -> {}
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection -> {}
                                                is ServerApi.OnlineGame.State.Round.Waiting -> {}
                                                is ServerApi.OnlineGame.State.Round.Preparation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.Explanation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.LastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.Editing -> {}
                                                is ServerApi.OnlineGame.State.GameResults -> {}
                                            }
                                        is ServerApi.OnlineGame.State.GameInitialisation ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.RoomPlayersGathering -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation -> {}
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection -> {}
                                                is ServerApi.OnlineGame.State.Round.Waiting -> {}
                                                is ServerApi.OnlineGame.State.Round.Preparation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.Explanation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.LastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.Editing -> {}
                                                is ServerApi.OnlineGame.State.GameResults -> {}
                                            }
                                        is ServerApi.OnlineGame.State.PlayersWordsCollection ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.RoomPlayersGathering -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation -> {}
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection -> {}
                                                is ServerApi.OnlineGame.State.Round.Waiting -> {}
                                                is ServerApi.OnlineGame.State.Round.Preparation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.Explanation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.LastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.Editing -> {}
                                                is ServerApi.OnlineGame.State.GameResults -> {}
                                            }
                                        is ServerApi.OnlineGame.State.Round.Waiting ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.RoomPlayersGathering -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation -> {}
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection -> {}
                                                is ServerApi.OnlineGame.State.Round.Waiting -> {}
                                                is ServerApi.OnlineGame.State.Round.Preparation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.Explanation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.LastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.Editing -> {}
                                                is ServerApi.OnlineGame.State.GameResults -> {}
                                            }
                                        is ServerApi.OnlineGame.State.Round.Preparation ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.RoomPlayersGathering ->
                                                    launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.GameInitialisation ->
                                                    launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection ->
                                                    launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.Round.Waiting ->
                                                    launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.Round.Preparation ->
                                                    if (previousState.roundNumber != newState.roundNumber || (previousState.millisecondsLeft / 1000u) != (newState.millisecondsLeft / 1000u))
                                                        launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.Round.Explanation ->
                                                    launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.Round.LastGuess ->
                                                    launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.Round.Editing ->
                                                    launch { settings.playPreparationCountdown() }
                                                is ServerApi.OnlineGame.State.GameResults ->
                                                    launch { settings.playPreparationCountdown() }
                                            }
                                        is ServerApi.OnlineGame.State.Round.Explanation ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.RoomPlayersGathering ->
                                                    launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.GameInitialisation ->
                                                    launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection ->
                                                    launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.Round.Waiting ->
                                                    launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.Round.Preparation ->
                                                    launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.Round.Explanation ->
                                                    if (previousState.roundNumber != newState.roundNumber)
                                                        launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.Round.LastGuess ->
                                                    launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.Round.Editing ->
                                                    launch { settings.playExplanationStart() }
                                                is ServerApi.OnlineGame.State.GameResults ->
                                                    launch { settings.playExplanationStart() }
                                            }
                                        is ServerApi.OnlineGame.State.Round.LastGuess ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.RoomPlayersGathering ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessStart() }
                                                    else
                                                        launch { settings.playFinalGuessEnd() }
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
                                                is ServerApi.OnlineGame.State.Round.Waiting ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessStart() }
                                                    else
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.Preparation ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessStart() }
                                                    else
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.Explanation ->
                                                    if (newState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessStart() }
                                                    else
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.LastGuess ->
                                                    if (newState.millisecondsLeft == 0u) {
                                                        launch { settings.playFinalGuessEnd() }
                                                    } else {
                                                        if (newState.roundNumber != previousState.roundNumber)
                                                            launch { settings.playFinalGuessStart() }
                                                    }
                                                is ServerApi.OnlineGame.State.Round.Editing ->
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
                                        is ServerApi.OnlineGame.State.Round.Editing ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.RoomPlayersGathering -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation -> {}
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection -> {}
                                                is ServerApi.OnlineGame.State.Round.Waiting -> {}
                                                is ServerApi.OnlineGame.State.Round.Preparation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.Explanation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.LastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.Editing -> {}
                                                is ServerApi.OnlineGame.State.GameResults -> {}
                                            }
                                        is ServerApi.OnlineGame.State.GameResults ->
                                            when (previousState) {
                                                null -> {}
                                                is ServerApi.OnlineGame.State.RoomPlayersGathering -> {}
                                                is ServerApi.OnlineGame.State.GameInitialisation -> {}
                                                is ServerApi.OnlineGame.State.PlayersWordsCollection -> {}
                                                is ServerApi.OnlineGame.State.Round.Waiting -> {}
                                                is ServerApi.OnlineGame.State.Round.Preparation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.Explanation ->
                                                    launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.LastGuess ->
                                                    if (previousState.millisecondsLeft > 0u)
                                                        launch { settings.playFinalGuessEnd() }
                                                is ServerApi.OnlineGame.State.Round.Editing -> {}
                                                is ServerApi.OnlineGame.State.GameResults -> {}
                                            }
                                    }
                                }
                                is ServerApi.Signal.OnlineGameError -> {} // TODO
                            }
                        }
                        cancel()
                    }
                } catch (exception: Exception) {
                    logger.warn(throwable = exception) { "Online game websocket connection exception" }
                    if (exception is CancellationException) throw exception
                }
                connectionStatus.value = ConnectionStatus.Disconnected
                gameStateFlow.value = null
                delay(300.milliseconds)
            }
        }
    }
}