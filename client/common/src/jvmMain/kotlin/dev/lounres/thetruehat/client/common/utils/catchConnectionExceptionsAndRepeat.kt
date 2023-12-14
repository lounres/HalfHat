package dev.lounres.thetruehat.client.common.utils

import kotlinx.coroutines.*
import java.net.ConnectException


public actual suspend fun CoroutineScope.catchConnectionExceptionsAndRepeat(tryBlock: suspend () -> Unit, catchBlock: suspend (exception: Exception) -> Unit) {
    try {
        tryBlock()
    } catch (e: ConnectException) {
        catchBlock(e)
    }
}