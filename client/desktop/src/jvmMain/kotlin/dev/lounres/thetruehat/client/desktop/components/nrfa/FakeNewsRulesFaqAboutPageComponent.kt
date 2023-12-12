package dev.lounres.thetruehat.client.desktop.components.nrfa

import dev.lounres.thetruehat.api.localization.Language


class FakeNewsRulesFaqAboutPageComponent(
    override val backButtonEnabled: Boolean = true,
): NewsRulesFaqAboutPageComponent {
    override val onBackButtonClick = {}
    override val onLanguageChange = { _: Language -> }
    override val onFeedbackButtonClick = {}

    override val onNewsButtonClick = {}
    override val onRulesButtonClick = {}
    override val onFaqButtonClick = {}
    override val onAboutButtonClick = {}
}