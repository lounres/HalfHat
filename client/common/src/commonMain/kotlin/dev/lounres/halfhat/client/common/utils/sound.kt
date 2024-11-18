package dev.lounres.halfhat.client.common.utils


public expect object DefaultSounds {
    public val preparationCountdown: ByteArray
    public val explanationStart: ByteArray
    public val finalGuessStart: ByteArray
    public val finalGuessEnd: ByteArray
}

public expect fun playSound(byteArray: ByteArray)