package dev.lounres.thetruehat.client.desktop.components.real

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.desktop.components.GamePageComponent
import dev.lounres.thetruehat.client.desktop.components.Language
import dev.lounres.thetruehat.client.desktop.components.RoundBreakPageComponent
import dev.lounres.thetruehat.client.desktop.components.RoundBreakUserRole


inline fun RealRoundBreakPageComponent(
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
    userRole: RoundBreakUserRole,
): RealRoundBreakPageComponent =
    object : RealRoundBreakPageComponent(
        userRole = userRole,
    ) {
        override val gamePageComponent: GamePageComponent =
            RealGamePageComponent(
                backButtonEnabled = backButtonEnabled,
                onBackButtonClick = onBackButtonClick,
                onLanguageChange = onLanguageChange,
                onFeedbackButtonClick = onFeedbackButtonClick,
                onHatButtonClick = onHatButtonClick,
                wordsNumber = wordsNumber,
                showFinishButton = showFinishButton,
                volumeOn = volumeOn,
                speakerNickname = speakerNickname,
                listenerNickname = listenerNickname,
                onVolumeButtonClick = onVolumeButtonClick,
                onFinishButtonClick = onFinishButtonClick,
                onExitButtonClick = onExitButtonClick,
            )
    }

abstract class RealRoundBreakPageComponent(
    override val userRole: RoundBreakUserRole
): RoundBreakPageComponent