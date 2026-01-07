package dev.lounres.halfhat.client.utils

import kotlinx.coroutines.Deferred


public expect class Audio

public expect suspend fun ByteArray.toAudio(): Audio

public expect object DefaultSounds {
    public val preparationCountdown: Deferred<ByteArray>
    public val explanationStart: Deferred<ByteArray>
    public val finalGuessStart: Deferred<ByteArray>
    public val finalGuessEnd: Deferred<ByteArray>
}

public expect fun Audio.play()