package dev.lounres.thetruehat.client.desktop.components


interface NewsRulesFaqAboutPageComponent {
    val theTrueHatPageComponent: TheTrueHatPageComponent

    fun onNewsButtonClick()
    fun onRulesButtonClick()
    fun onFaqButtonClick()
    fun onAboutButtonClick()
}