package dev.lounres.halfhat.client.common.utils

import dev.lounres.halfhat.client.common.resources.Res
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async


public object DefaultSounds {
    public val preparationCountdown: Deferred<ByteArray> = CoroutineScope(Dispatchers.Default).async { Res.readBytes("files/sounds/countdown.wav") }
    public val explanationStart: Deferred<ByteArray> = CoroutineScope(Dispatchers.Default).async { Res.readBytes("files/sounds/explanationStart.wav") }
    public val finalGuessStart: Deferred<ByteArray> = CoroutineScope(Dispatchers.Default).async { Res.readBytes("files/sounds/finalGuessStart.wav") }
    public val finalGuessEnd: Deferred<ByteArray> = CoroutineScope(Dispatchers.Default).async { Res.readBytes("files/sounds/finalGuessEnd.wav") }
}

public expect suspend fun ByteArray.playSound()