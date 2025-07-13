package dev.lounres.halfhat.client.common.utils

import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.kone.scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import javax.sound.sampled.*
import javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED
import javax.sound.sampled.SourceDataLine


public actual typealias Audio = ByteArray

public actual object DefaultSounds {
    public actual val preparationCountdown: Deferred<Audio> =
        CoroutineScope(Dispatchers.Default).async { Res.readBytes("files/sounds/countdown.mp3") }
    public actual val explanationStart: Deferred<Audio> =
        CoroutineScope(Dispatchers.Default).async { Res.readBytes("files/sounds/explanationStart.mp3") }
    public actual val finalGuessStart: Deferred<Audio> =
        CoroutineScope(Dispatchers.Default).async { Res.readBytes("files/sounds/finalGuessStart.mp3") }
    public actual val finalGuessEnd: Deferred<Audio> =
        CoroutineScope(Dispatchers.Default).async { Res.readBytes("files/sounds/finalGuessEnd.mp3") }
}

public actual fun Audio.play() {
    AudioSystem.getAudioInputStream(this.inputStream()).use { audioInputStream ->
        val outFormat = scope {
            val inFormat = audioInputStream.format
            val ch = inFormat.getChannels()
            val rate = inFormat.getSampleRate()
            AudioFormat(PCM_SIGNED, rate, 16, ch, ch * 2, rate, false)
        }
        val info = DataLine.Info(SourceDataLine::class.java, outFormat)
        (AudioSystem.getLine(info) as SourceDataLine).let { line ->
            try {
                line.open(outFormat)
                line.start()
                scope {
                    val audioInputStream = AudioSystem.getAudioInputStream(outFormat, audioInputStream)
                    val buffer = ByteArray(4096)
                    var n = 0
                    while (n != -1) {
                        line.write(buffer, 0, n)
                        n = audioInputStream.read(buffer, 0, buffer.size)
                    }
                }
                line.drain()
                line.stop()
            } finally {
                line.close()
            }
        }
    }
}