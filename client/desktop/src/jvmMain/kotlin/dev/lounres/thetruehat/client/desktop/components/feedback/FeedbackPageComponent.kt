package dev.lounres.thetruehat.client.desktop.components.feedback

import dev.lounres.thetruehat.api.localization.Language


interface FeedbackPageComponent {
    val backButtonEnabled: Boolean
    val onBackButtonClick: () -> Unit
    val onLanguageChange: (Language) -> Unit
    val onFeedbackButtonClick: () -> Unit
    val onHatButtonClick: () -> Unit
    val sendFeedback: (feedback: String, sendAdditionalData: Boolean) -> Unit
}