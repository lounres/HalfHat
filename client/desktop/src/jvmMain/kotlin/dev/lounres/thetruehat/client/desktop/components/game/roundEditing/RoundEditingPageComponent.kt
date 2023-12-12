package dev.lounres.thetruehat.client.desktop.components.game.roundEditing

import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.client.common.models.MutableExplanationResult


interface RoundEditingPageComponent {
    val backButtonEnabled: Boolean
    val wordsNumber: Int
    val volumeOn: Boolean
    val showFinishButton: Boolean
    val onBackButtonClick: () -> Unit
    val onLanguageChange: (Language) -> Unit
    val onFeedbackButtonClick: () -> Unit
    val onHatButtonClick: () -> Unit
    val onExitButtonClick: () -> Unit
    val onVolumeButtonClick: () -> Unit
    val onFinishButtonClick: () -> Unit

    val explanationResults: List<MutableExplanationResult>
}