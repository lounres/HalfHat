package dev.lounres.thetruehat.client.desktop.components.feedback

import dev.lounres.thetruehat.api.localization.Language


class FakeFeedbackPageComponent (
    override val backButtonEnabled: Boolean = true,
): FeedbackPageComponent {
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (Language) -> Unit = { _ -> }
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}
    override val sendFeedback: (feedback: String, sendAdditionalData: Boolean) -> Unit = { _, _ ->  }
}