package dev.lounres.halfhat.client.common.utils

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds


public expect val defaultHttpClientEngine: HttpClientEngineFactory<HttpClientEngineConfig>

public val defaultHttpClient: HttpClient by lazy {
    HttpClient(defaultHttpClientEngine) {
        WebSockets {
            pingInterval = 1000.milliseconds
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }
}