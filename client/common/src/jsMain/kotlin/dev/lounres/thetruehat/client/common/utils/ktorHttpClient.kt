package dev.lounres.thetruehat.client.common.utils

import io.ktor.client.engine.*
import io.ktor.client.engine.js.*


public actual val defaultHttpClientEngine: HttpClientEngineFactory<HttpClientEngineConfig> = Js