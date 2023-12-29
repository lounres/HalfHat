package dev.lounres.thetruehat.client.common.utils

import javax.sound.sampled.AudioSystem


public actual fun playSound(pathToTheSound: String) {
    val audioInputStream = AudioSystem.getAudioInputStream({}::class.java.getResource(pathToTheSound))
    val clip = AudioSystem.getClip()
    clip.open(audioInputStream)
    clip.start()
}