package dev.lounres.thetruehat.client.desktop.components


enum class Language {
    Russian, English
}

interface TheTrueHatPageComponent {
    val backButtonEnabled: Boolean
    fun onBackButtonClick()

    fun onLanguageChange(language: Language)

    fun onFeedbackButtonClick()
}