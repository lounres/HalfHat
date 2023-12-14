package dev.lounres.thetruehat.client.common.utils

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.json.Json


public expect val defaultHttpClientEngine: HttpClientEngineFactory<HttpClientEngineConfig>

public val defaultHttpClient: HttpClient
    get() = HttpClient(defaultHttpClientEngine) { // TODO: Move it somewhere else
        WebSockets {
            pingInterval = 1000
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }