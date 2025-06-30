package dev.lounres.halfhat.client.common.utils

import korlibs.audio.format.WAV
import korlibs.audio.sound.toSound


public actual suspend fun ByteArray.playSound() {
    WAV.decode(this)!!.toSound().play()
}