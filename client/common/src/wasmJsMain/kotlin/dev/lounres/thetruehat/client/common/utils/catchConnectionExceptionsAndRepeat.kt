package dev.lounres.halfhat.client.common.utils

import kotlinx.coroutines.*


public actual suspend fun CoroutineScope.catchConnectionExceptionsAndRepeat(tryBlock: suspend () -> Unit, catchBlock: suspend (exception: Exception) -> Unit) {
    try {
        tryBlock()
    } catch (e: Exception /* TODO: Substitute with more accurate exception class */) {
        catchBlock(e)
    }
}