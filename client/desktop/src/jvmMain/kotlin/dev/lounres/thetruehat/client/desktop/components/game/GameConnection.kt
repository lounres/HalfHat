package dev.lounres.thetruehat.client.desktop.components.game

import dev.lounres.thetruehat.api.ClientSignal
import dev.lounres.thetruehat.api.ServerSignal
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.*
import java.net.ConnectException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel


class GameConnection(
    val coroutineScope: CoroutineScope,
    val httpClient: HttpClient,
    val host: String? = null,
    val port: Int? = null,
    val path: String? = null,
    val retryPeriod: Long,
    val onConnect: suspend DefaultClientWebSocketSession.() -> Unit,
    val onConnectionFailure: suspend () -> Unit,
): AutoCloseable {
    private val _incoming: Channel<ServerSignal> = Channel(capacity = Channel.UNLIMITED)
    val incoming: ReceiveChannel<ServerSignal> = _incoming
    private val _outgoing: Channel<ClientSignal> = Channel(capacity = Channel.UNLIMITED)
    val outgoing: SendChannel<ClientSignal> = _outgoing

    val connectionJob = coroutineScope.launch {
        while (isActive) {
            try {
                httpClient.ws(host = host, port = port, path = path) {
                    onConnect()
                    val incomingJob = launch {
                        for (frame in incoming) {
                            val converter = converter!!

                            if (!converter.isApplicable(frame)) continue

                            val signal = converter.deserialize<ServerSignal>(content = frame)
                            println("Incoming signal: $signal")
                            _incoming.send(signal)
                        }
                    }
                    val outgoingJob = launch {
                        for (signal in _outgoing) sendSerialized<ClientSignal>(signal)
                    }
                    listOf(incomingJob, outgoingJob).joinAll()
                }
            } catch (e: ConnectException) {
                println(e)
                onConnectionFailure()
            }

            delay(retryPeriod)
        }
    }

    override fun close() {
        connectionJob.cancel()
    }
}