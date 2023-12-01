package dev.lounres.thetruehat.client.desktop.components.real

import dev.lounres.thetruehat.client.desktop.components.HomePageComponent
import dev.lounres.thetruehat.client.desktop.components.Language
import dev.lounres.thetruehat.client.desktop.components.TheTrueHatPageWithHatComponent


inline fun RealHomePageComponent(
    backButtonEnabled: Boolean,
    crossinline onBackButtonClick: () -> Unit,
    crossinline onLanguageChange: (language: Language) -> Unit,
    crossinline onFeedbackButtonClick: () -> Unit,
    crossinline onHatButtonClick: () -> Unit,
    crossinline onCreateButtonClick: () -> Unit,
    crossinline onEnterButtonClick: () -> Unit,
): RealHomePageComponent =
    object : RealHomePageComponent() {
        override val theTrueHatPageWithHatComponent: TheTrueHatPageWithHatComponent =
            RealTheTrueHatPageWithHatComponent(
                backButtonEnabled = backButtonEnabled,
                onBackButtonClick = onBackButtonClick,
                onLanguageChange = onLanguageChange,
                onFeedbackButtonClick = onFeedbackButtonClick,
                onHatButtonClick = onHatButtonClick,
            )

        override fun onCreateButtonClick() {
            onCreateButtonClick()
        }

        override fun onEnterButtonClick() {
            onEnterButtonClick()
        }
    }

abstract class RealHomePageComponent: HomePageComponent