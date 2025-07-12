package dev.lounres.halfhat.client.common.utils

import dev.lounres.halfhat.client.common.resources.Res
import js.buffer.ArrayBuffer
import js.core.JsPrimitives.toJsByte
import js.typedarrays.Int8Array
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import web.audio.AudioBuffer
import web.audio.AudioContext
import web.audio.decodeAudioData

public actual typealias Audio = AudioBuffer

private val context = AudioContext()

private suspend fun ByteArray.toAudio(): Audio =
    context.decodeAudioData(
        ArrayBuffer(this.size).also {
            val view = Int8Array(it)
            this.forEachIndexed { index, b -> view[index] = b.toJsByte() }
        }
    )

public actual object DefaultSounds {
    public actual val preparationCountdown: Deferred<Audio> =
        CoroutineScope(Dispatchers.Default).async(start = CoroutineStart.LAZY) { Res.readBytes("files/sounds/countdown.wav").toAudio() }
    public actual val explanationStart: Deferred<Audio> =
        CoroutineScope(Dispatchers.Default).async(start = CoroutineStart.LAZY) { Res.readBytes("files/sounds/explanationStart.wav").toAudio() }
    public actual val finalGuessStart: Deferred<Audio> =
        CoroutineScope(Dispatchers.Default).async(start = CoroutineStart.LAZY) { Res.readBytes("files/sounds/finalGuessStart.wav").toAudio() }
    public actual val finalGuessEnd: Deferred<Audio> =
        CoroutineScope(Dispatchers.Default).async(start = CoroutineStart.LAZY) { Res.readBytes("files/sounds/finalGuessEnd.wav").toAudio() }
}

public actual suspend fun Audio.play() {
    val source = context.createBufferSource()
    source.buffer = this
    source.connect(context.destination)
    source.start()
}