package dev.lounres.thetruehat.client.common.components.nrfa

import dev.lounres.thetruehat.api.localization.Language


public interface NewsRulesFaqAboutPageComponent {
    public val backButtonEnabled: Boolean
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (language: Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onNewsButtonClick: () -> Unit
    public val onRulesButtonClick: () -> Unit
    public val onFaqButtonClick: () -> Unit
    public val onAboutButtonClick: () -> Unit
}