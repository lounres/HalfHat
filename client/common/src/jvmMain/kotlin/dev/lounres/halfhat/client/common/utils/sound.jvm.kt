package dev.lounres.halfhat.client.common.utils

import dev.lounres.halfhat.client.common.resources.Res
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem


@OptIn(ExperimentalResourceApi::class)
public actual object DefaultSounds {
    public actual val preparationCountdown: ByteArray = runBlocking { Res.readBytes("files/countdown.wav") }
    public actual val explanationStart: ByteArray = runBlocking { Res.readBytes("files/explanationStart.wav") }
    public actual val finalGuessStart: ByteArray = runBlocking { Res.readBytes("files/finalGuessStart.wav") }
    public actual val finalGuessEnd: ByteArray = runBlocking { Res.readBytes("files/finalGuessEnd.wav") }
}

// Plays only `.wav` format
public actual fun playSound(byteArray: ByteArray) {
    val audioInputStream = AudioSystem.getAudioInputStream(ByteArrayInputStream(byteArray))
    val clip = AudioSystem.getClip()
    clip.open(audioInputStream)
    clip.start()
}