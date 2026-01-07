package dev.lounres.halfhat.client.utils

import dev.lounres.halfhat.client.resources.Res
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

public actual suspend fun ByteArray.toAudio(): Audio =
    context.decodeAudioData(
        ArrayBuffer(this.size).also {
            val view = Int8Array(it)
            this.forEachIndexed { index, b -> view[index] = b.toJsByte() }
        }
    )

private val soundCoroutineScope = CoroutineScope(Dispatchers.Default)

public actual object DefaultSounds {
    public actual val preparationCountdown: Deferred<ByteArray> =
        soundCoroutineScope.async(start = CoroutineStart.LAZY) { Res.readBytes("files/sounds/countdown.mp3") }
    public actual val explanationStart: Deferred<ByteArray> =
        soundCoroutineScope.async(start = CoroutineStart.LAZY) { Res.readBytes("files/sounds/explanationStart.mp3") }
    public actual val finalGuessStart: Deferred<ByteArray> =
        soundCoroutineScope.async(start = CoroutineStart.LAZY) { Res.readBytes("files/sounds/finalGuessStart.mp3") }
    public actual val finalGuessEnd: Deferred<ByteArray> =
        soundCoroutineScope.async(start = CoroutineStart.LAZY) { Res.readBytes("files/sounds/finalGuessEnd.mp3") }
}

public actual fun Audio.play() {
    val source = context.createBufferSource()
    source.buffer = this
    source.connect(context.destination)
    source.start()
}