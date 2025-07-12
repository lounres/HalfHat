package dev.lounres.halfhat.client.common.utils

import dev.lounres.halfhat.client.common.resources.Res
import korlibs.audio.format.WAV
import korlibs.audio.sound.toSound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async


public actual typealias Audio = ByteArray

public actual object DefaultSounds {
    public actual val preparationCountdown: Deferred<Audio> =
        CoroutineScope(Dispatchers.Default).async { Res.readBytes("files/sounds/countdown.wav") }
    public actual val explanationStart: Deferred<Audio> =
        CoroutineScope(Dispatchers.Default).async { Res.readBytes("files/sounds/explanationStart.wav") }
    public actual val finalGuessStart: Deferred<Audio> =
        CoroutineScope(Dispatchers.Default).async { Res.readBytes("files/sounds/finalGuessStart.wav") }
    public actual val finalGuessEnd: Deferred<Audio> =
        CoroutineScope(Dispatchers.Default).async { Res.readBytes("files/sounds/finalGuessEnd.wav") }
}

public actual suspend fun ByteArray.play() {
//    WAV.decode(this)!!.toSound().play()
}