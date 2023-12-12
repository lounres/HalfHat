package dev.lounres.thetruehat.client.desktop.components.game.roundEditing

import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription
import dev.lounres.thetruehat.client.common.models.MutableExplanationResult


class FakeRoundEditingPageComponent(
    override val backButtonEnabled: Boolean = true,
    override val wordsNumber: Int = 100,
    override val volumeOn: Boolean = true,
    override val showFinishButton: Boolean = true,
    override val explanationResults: List<MutableExplanationResult> = listOf(
        MutableExplanationResult(word = "реабилитация", RoomDescription.WordExplanationResult.State.Explained),
        MutableExplanationResult(word = "социализация", RoomDescription.WordExplanationResult.State.NotExplained),
        MutableExplanationResult(word = "кастомизация", RoomDescription.WordExplanationResult.State.Mistake),
    ),
) : RoundEditingPageComponent {
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (Language) -> Unit = {}
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}
    override val onExitButtonClick: () -> Unit = {}
    override val onVolumeButtonClick: () -> Unit = {}
    override val onFinishButtonClick: () -> Unit = {}
}