package dev.lounres.thetruehat.client.desktop.components.real

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.desktop.components.GamePageComponent
import dev.lounres.thetruehat.client.desktop.components.Language
import dev.lounres.thetruehat.client.desktop.components.TheTrueHatPageWithHatComponent


inline fun RealGamePageComponent(
    backButtonEnabled: Boolean,
    crossinline onBackButtonClick: () -> Unit,
    crossinline onLanguageChange: (language: Language) -> Unit,
    crossinline onFeedbackButtonClick: () -> Unit,
    crossinline onHatButtonClick: () -> Unit,
    wordsNumber: Value<Int>,
    showFinishButton: Value<Boolean>,
    volumeOn: Value<Boolean>,
    speakerNickname: Value<String>,
    listenerNickname: Value<String>,
    crossinline onVolumeButtonClick: () -> Unit,
    crossinline onFinishButtonClick: () -> Unit,
    crossinline onExitButtonClick: () -> Unit,
): RealGamePageComponent =
    object : RealGamePageComponent(
        wordsNumber = wordsNumber,
        showFinishButton = showFinishButton,
        volumeOn = volumeOn,
        speakerNickname = speakerNickname,
        listenerNickname = listenerNickname,
    ) {
        override val theTrueHatPageWithHatComponent: TheTrueHatPageWithHatComponent =
            RealTheTrueHatPageWithHatComponent(
                backButtonEnabled = backButtonEnabled,
                onBackButtonClick = onBackButtonClick,
                onLanguageChange = onLanguageChange,
                onFeedbackButtonClick = onFeedbackButtonClick,
                onHatButtonClick = onHatButtonClick,
            )

        override fun onVolumeButtonClick() = onVolumeButtonClick()

        override fun onFinishButtonClick() = onFinishButtonClick()

        override fun onExitButtonClick() = onExitButtonClick()
    }

abstract class RealGamePageComponent(
    override val wordsNumber: Value<Int>,
    override val showFinishButton: Value<Boolean>,
    override val volumeOn: Value<Boolean>,
    override val speakerNickname: Value<String>,
    override val listenerNickname: Value<String>,
): GamePageComponent