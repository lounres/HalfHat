package dev.lounres.thetruehat.client.common.components.nrfa

import dev.lounres.thetruehat.api.localization.Language


public class FakeNewsRulesFaqAboutPageComponent(
    override val backButtonEnabled: Boolean = true,
): NewsRulesFaqAboutPageComponent {
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (language: Language) -> Unit = { _: Language -> }
    override val onFeedbackButtonClick: () -> Unit = {}

    override val onNewsButtonClick: () -> Unit = {}
    override val onRulesButtonClick: () -> Unit = {}
    override val onFaqButtonClick: () -> Unit = {}
    override val onAboutButtonClick: () -> Unit = {}
}