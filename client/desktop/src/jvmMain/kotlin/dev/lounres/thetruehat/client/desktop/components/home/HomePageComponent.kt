package dev.lounres.thetruehat.client.desktop.components.home

import dev.lounres.thetruehat.api.localization.Language


interface HomePageComponent {
    val backButtonEnabled: Boolean
    val onBackButtonClick: () -> Unit
    val onLanguageChange: (language: Language) -> Unit
    val onFeedbackButtonClick: () -> Unit
    val onHatButtonClick: () -> Unit
    val onCreateButtonClick: () -> Unit
    val onEnterButtonClick: () -> Unit
}