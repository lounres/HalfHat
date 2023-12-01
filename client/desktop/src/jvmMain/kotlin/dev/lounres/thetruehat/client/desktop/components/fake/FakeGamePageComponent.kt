package dev.lounres.thetruehat.client.desktop.components.fake

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.desktop.components.GamePageComponent


class FakeGamePageComponent(
    backButtonEnabled: Boolean = true,
    wordsNumber: Int = 100,
    showFinishButton: Boolean = true,
    volumeOn: Boolean = true,
    speakerNickname: String = "Panther",
    listenerNickname: String = "Jaguar",
): GamePageComponent {
    override val theTrueHatPageWithHatComponent = FakeTheTrueHatPageWithHatComponent(
        backButtonEnabled = backButtonEnabled
    )

    override val wordsNumber: Value<Int> = MutableValue(wordsNumber)
    override val showFinishButton: Value<Boolean> = MutableValue(showFinishButton)
    override val volumeOn: Value<Boolean> = MutableValue(volumeOn)
    override val speakerNickname: Value<String> = MutableValue(speakerNickname)
    override val listenerNickname: Value<String> = MutableValue(listenerNickname)
    override fun onVolumeButtonClick() {}
    override fun onFinishButtonClick() {}
    override fun onExitButtonClick() {}
}