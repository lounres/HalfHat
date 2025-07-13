package dev.lounres.halfhat.client.common.utils

import dev.lounres.halfhat.client.common.resources.Res
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import javax.sound.sampled.AudioSystem


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

public actual fun Audio.play() {
    val clip = AudioSystem.getClip()
    clip.open(AudioSystem.getAudioInputStream(this.inputStream()))
    clip.framePosition = 0
    clip.start()
}