package dev.lounres.thetruehat.client.desktop.components.real

import dev.lounres.thetruehat.client.desktop.components.Language
import dev.lounres.thetruehat.client.desktop.components.NewsRulesFaqAboutPageComponent
import dev.lounres.thetruehat.client.desktop.components.TheTrueHatPageComponent


inline fun RealNewsRulesFaqAboutPageComponent(
    backButtonEnabled: Boolean,
    crossinline onBackButtonClick: () -> Unit,
    crossinline onLanguageChange: (language: Language) -> Unit,
    crossinline onFeedbackButtonClick: () -> Unit,
    crossinline onNewsButtonClick: () -> Unit,
    crossinline onRulesButtonClick: () -> Unit,
    crossinline onFaqButtonClick: () -> Unit,
    crossinline onAboutButtonClick: () -> Unit,
): RealNewsRulesFaqAboutPageComponent =
    object : RealNewsRulesFaqAboutPageComponent() {
        override val theTrueHatPageComponent: TheTrueHatPageComponent =
            RealTheTrueHatPageComponent(
                backButtonEnabled = backButtonEnabled,
                onBackButtonClick = onBackButtonClick,
                onLanguageChange = onLanguageChange,
                onFeedbackButtonClick = onFeedbackButtonClick,
            )

        override fun onNewsButtonClick() = onNewsButtonClick()
        override fun onRulesButtonClick() = onRulesButtonClick()
        override fun onFaqButtonClick() = onFaqButtonClick()
        override fun onAboutButtonClick() = onAboutButtonClick()
    }

abstract class RealNewsRulesFaqAboutPageComponent: NewsRulesFaqAboutPageComponent {
}