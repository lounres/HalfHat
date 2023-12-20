package dev.lounres.thetruehat.client.common.utils

import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.*


public actual suspend fun CoroutineScope.catchConnectionExceptionsAndRepeat(tryBlock: suspend () -> Unit, catchBlock: suspend (exception: Exception) -> Unit) {
    try {
        tryBlock()
    } catch (e: WebSocketException) {
        catchBlock(e)
    }
}