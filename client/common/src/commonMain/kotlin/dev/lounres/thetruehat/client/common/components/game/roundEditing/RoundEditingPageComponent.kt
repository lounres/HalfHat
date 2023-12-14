package dev.lounres.thetruehat.client.common.components.game.roundEditing

import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.client.common.models.MutableExplanationResult


public interface RoundEditingPageComponent {
    public val backButtonEnabled: Boolean
    public val wordsNumber: Int
    public val volumeOn: Boolean
    public val showFinishButton: Boolean
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit
    public val onExitButtonClick: () -> Unit
    public val onVolumeButtonClick: () -> Unit
    public val onFinishButtonClick: () -> Unit

    public val explanationResults: List<MutableExplanationResult>
}