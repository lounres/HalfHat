package dev.lounres.thetruehat.client.common.utils

import kotlinx.coroutines.*


public expect suspend fun CoroutineScope.catchConnectionExceptionsAndRepeat(tryBlock: suspend () -> Unit, catchBlock: suspend (exception: Exception) -> Unit)