package dev.lounres.halfhat.client.common.utils

import kotlinx.coroutines.Deferred


public expect class Audio

public expect object DefaultSounds {
    public val preparationCountdown: Deferred<Audio>
    public val explanationStart: Deferred<Audio>
    public val finalGuessStart: Deferred<Audio>
    public val finalGuessEnd: Deferred<Audio>
}

public expect fun Audio.play()