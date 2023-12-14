package dev.lounres.thetruehat.client.common.utils

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*


public actual val defaultHttpClientEngine: HttpClientEngineFactory<HttpClientEngineConfig> = CIO