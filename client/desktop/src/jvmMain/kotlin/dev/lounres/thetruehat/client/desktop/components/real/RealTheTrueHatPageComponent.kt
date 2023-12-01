package dev.lounres.thetruehat.client.desktop.components.real

import dev.lounres.thetruehat.client.desktop.components.Language
import dev.lounres.thetruehat.client.desktop.components.TheTrueHatPageComponent


inline fun RealTheTrueHatPageComponent(
    backButtonEnabled: Boolean,
    crossinline onBackButtonClick: () -> Unit,
    crossinline onLanguageChange: (language: Language) -> Unit,
    crossinline onFeedbackButtonClick: () -> Unit,
): RealTheTrueHatPageComponent =
    object : RealTheTrueHatPageComponent(backButtonEnabled = backButtonEnabled) {
        override fun onBackButtonClick() = onBackButtonClick()
        override fun onLanguageChange(language: Language) = onLanguageChange(language)
        override fun onFeedbackButtonClick() = onFeedbackButtonClick()
    }

abstract class RealTheTrueHatPageComponent(
    override val backButtonEnabled: Boolean,
): TheTrueHatPageComponent