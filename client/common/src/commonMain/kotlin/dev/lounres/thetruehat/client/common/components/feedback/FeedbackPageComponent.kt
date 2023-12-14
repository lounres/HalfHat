package dev.lounres.thetruehat.client.common.components.feedback

import dev.lounres.thetruehat.api.localization.Language


public interface FeedbackPageComponent {
    public val backButtonEnabled: Boolean
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit
    public val sendFeedback: (feedback: String, sendAdditionalData: Boolean) -> Unit
}