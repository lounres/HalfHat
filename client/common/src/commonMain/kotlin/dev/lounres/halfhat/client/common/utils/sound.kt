package dev.lounres.halfhat.client.common.utils

import dev.lounres.halfhat.client.common.resources.Res
import korlibs.audio.format.WAV
import korlibs.audio.sound.Sound
import korlibs.audio.sound.toSound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async


public object DefaultSounds {
    public val preparationCountdown: Deferred<Sound> = CoroutineScope(Dispatchers.Default).async { WAV.decode(Res.readBytes("files/sounds/countdown.wav"))!!.toSound() }
    public val explanationStart: Deferred<Sound> = CoroutineScope(Dispatchers.Default).async { WAV.decode(Res.readBytes("files/sounds/explanationStart.wav"))!!.toSound() }
    public val finalGuessStart: Deferred<Sound> = CoroutineScope(Dispatchers.Default).async { WAV.decode(Res.readBytes("files/sounds/finalGuessStart.wav"))!!.toSound() }
    public val finalGuessEnd: Deferred<Sound> = CoroutineScope(Dispatchers.Default).async { WAV.decode(Res.readBytes("files/sounds/finalGuessEnd.wav"))!!.toSound() }
}