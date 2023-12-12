package dev.lounres.thetruehat.client.desktop.components.nrfa

import dev.lounres.thetruehat.api.localization.Language


interface NewsRulesFaqAboutPageComponent {
    val backButtonEnabled: Boolean
    val onBackButtonClick: () -> Unit
    val onLanguageChange: (language: Language) -> Unit
    val onFeedbackButtonClick: () -> Unit
    val onNewsButtonClick: () -> Unit
    val onRulesButtonClick: () -> Unit
    val onFaqButtonClick: () -> Unit
    val onAboutButtonClick: () -> Unit
}