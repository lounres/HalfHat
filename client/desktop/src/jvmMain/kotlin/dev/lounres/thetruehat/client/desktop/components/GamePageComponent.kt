package dev.lounres.thetruehat.client.desktop.components

import com.arkivanov.decompose.value.Value


interface GamePageComponent {
    val theTrueHatPageWithHatComponent: TheTrueHatPageWithHatComponent

    val wordsNumber: Value<Int>
    val showFinishButton: Value<Boolean>
    val volumeOn: Value<Boolean>
    val speakerNickname: Value<String>
    val listenerNickname: Value<String>
    fun onVolumeButtonClick()
    fun onFinishButtonClick()
    fun onExitButtonClick()
}