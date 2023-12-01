package dev.lounres.thetruehat.client.desktop.components.fake

import dev.lounres.thetruehat.client.desktop.components.NewsRulesFaqAboutPageComponent


class FakeNewsRulesFaqAboutPageComponent(
    backButtonEnabled: Boolean = true,
): NewsRulesFaqAboutPageComponent {
    override val theTrueHatPageComponent = FakeTheTrueHatPageComponent(
        backButtonEnabled = backButtonEnabled
    )

    override fun onNewsButtonClick() {}
    override fun onRulesButtonClick() {}
    override fun onFaqButtonClick() {}
    override fun onAboutButtonClick() {}
}