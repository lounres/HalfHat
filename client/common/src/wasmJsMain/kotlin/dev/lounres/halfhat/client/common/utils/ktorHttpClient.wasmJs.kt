package dev.lounres.halfhat.client.common.utils

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

public actual val defaultHttpClientEngine: HttpClientEngineFactory<HttpClientEngineConfig> = Js