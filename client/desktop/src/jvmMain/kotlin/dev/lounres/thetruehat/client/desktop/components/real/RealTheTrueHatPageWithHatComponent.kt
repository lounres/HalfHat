package dev.lounres.thetruehat.client.desktop.components.real

import dev.lounres.thetruehat.client.desktop.components.Language
import dev.lounres.thetruehat.client.desktop.components.TheTrueHatPageComponent
import dev.lounres.thetruehat.client.desktop.components.TheTrueHatPageWithHatComponent


inline fun RealTheTrueHatPageWithHatComponent(
    backButtonEnabled: Boolean,
    crossinline onBackButtonClick: () -> Unit,
    crossinline onLanguageChange: (language: Language) -> Unit,
    crossinline onFeedbackButtonClick: () -> Unit,
    crossinline onHatButtonClick: () -> Unit,
): RealTheTrueHatPageWithHatComponent =
    object : RealTheTrueHatPageWithHatComponent() {
        override val theTrueHatPageComponent: TheTrueHatPageComponent =
            RealTheTrueHatPageComponent(
                backButtonEnabled = backButtonEnabled,
                onBackButtonClick = onBackButtonClick,
                onLanguageChange = onLanguageChange,
                onFeedbackButtonClick = onFeedbackButtonClick,
            )

        override fun onHatButtonClick() = onHatButtonClick()
    }

abstract class RealTheTrueHatPageWithHatComponent: TheTrueHatPageWithHatComponent