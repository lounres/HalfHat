package dev.lounres.thetruehat.client.common.components.home

import dev.lounres.thetruehat.api.localization.Language


public class FakeHomePageComponent(
    override val backButtonEnabled: Boolean = true
): HomePageComponent {
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (language: Language) -> Unit = { _: Language -> }
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}

    override val onCreateButtonClick: () -> Unit = {}
    override val onEnterButtonClick: () -> Unit = {}
}