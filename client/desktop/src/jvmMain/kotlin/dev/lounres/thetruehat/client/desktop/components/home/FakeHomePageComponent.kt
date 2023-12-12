package dev.lounres.thetruehat.client.desktop.components.home

import dev.lounres.thetruehat.api.localization.Language


class FakeHomePageComponent(
    override val backButtonEnabled: Boolean = true
): HomePageComponent {
    override val onBackButtonClick = {}
    override val onLanguageChange = { _: Language -> }
    override val onFeedbackButtonClick = {}
    override val onHatButtonClick = {}

    override val onCreateButtonClick = {}
    override val onEnterButtonClick = {}
}