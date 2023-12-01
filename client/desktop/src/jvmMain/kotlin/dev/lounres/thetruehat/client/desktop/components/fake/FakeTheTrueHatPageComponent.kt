package dev.lounres.thetruehat.client.desktop.components.fake

import dev.lounres.thetruehat.client.desktop.components.Language
import dev.lounres.thetruehat.client.desktop.components.TheTrueHatPageComponent


class FakeTheTrueHatPageComponent(
    override val backButtonEnabled: Boolean = true
): TheTrueHatPageComponent {
    override fun onBackButtonClick() {}

    override fun onLanguageChange(language: Language) {}

    override fun onFeedbackButtonClick() {}
}