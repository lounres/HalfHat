package dev.lounres.thetruehat.client.desktop

import dev.lounres.thetruehat.api.ClientSignal
import dev.lounres.thetruehat.api.ServerSignal
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module


class WebSocketConnection(
    val httpClient: HttpClient,
    val host: String? = null,
    val port: Int? = null,
    val path: String? = null,
    val retryPeriod: Long,
): AutoCloseable {
    private val _incoming: Channel<ServerSignal> = Channel(capacity = Channel.UNLIMITED)
    val incoming: ReceiveChannel<ServerSignal> = _incoming
    private val _outgoing: Channel<ClientSignal> = Channel(capacity = Channel.UNLIMITED)
    val outgoing: SendChannel<ClientSignal> = _outgoing

    @OptIn(DelicateCoroutinesApi::class)
    val connectionJob = GlobalScope.launch {
        while (isActive) {
            try {
                httpClient.webSocket(host = host, port = port, path = path) {
                    val incomingJob = launch {
                        for (frame in incoming) {
                            val converter = converter!!

                            if (!converter.isApplicable(frame)) continue

                            val signal = converter.deserialize<ServerSignal>(content = frame)
                            _incoming.send(signal)
                        }
                    }
                    val outgoingJob = launch {
                        for (signal in _outgoing) sendSerialized<ClientSignal>(signal)
                    }
                    listOf(incomingJob, outgoingJob).joinAll()
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            }
            delay(retryPeriod)
        }
    }

    override fun close() {
        connectionJob.cancel()
    }
}

class GameApplication: KoinComponent {
    val connection: WebSocketConnection by inject()
}

val httpModule = module {
    single<HttpClient> {
        HttpClient(CIO) {
            WebSockets {
                pingInterval = 1000
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }
    }
    factory<WebSocketConnection> {
        WebSocketConnection(
            httpClient = get(),
            host = getPropertyOrNull("host"),
            port = getPropertyOrNull<String>("port")?.toInt(),
            path = getPropertyOrNull("path"),
            retryPeriod = getProperty<String>("retryPeriod").toLong(),
        )
    }
}


fun main() {

}