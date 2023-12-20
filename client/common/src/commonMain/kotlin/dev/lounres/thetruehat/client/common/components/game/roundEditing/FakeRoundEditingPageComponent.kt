package dev.lounres.thetruehat.client.common.components.game.roundEditing

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription


public class FakeRoundEditingPageComponent(
    override val backButtonEnabled: Boolean = true,
    override val unitsUntilEnd: Value<RoomDescription.UnitsUntilEnd> = MutableValue(RoomDescription.UnitsUntilEnd.Words(100)),
    override val volumeOn: Value<Boolean> = MutableValue(true),
    override val showFinishButton: Value<Boolean> = MutableValue(true),
    override val explanationResults: List<MutableValue<RoomDescription.WordExplanationResult>>? = listOf(
        MutableValue(RoomDescription.WordExplanationResult(word = "реабилитация", RoomDescription.WordExplanationResult.State.Explained)),
        MutableValue(RoomDescription.WordExplanationResult(word = "социализация", RoomDescription.WordExplanationResult.State.NotExplained)),
        MutableValue(RoomDescription.WordExplanationResult(word = "кастомизация", RoomDescription.WordExplanationResult.State.Mistake)),
    ),
) : RoundEditingPageComponent {
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (Language) -> Unit = {}
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}
    override val onExitButtonClick: () -> Unit = {}
    override val onVolumeButtonClick: () -> Unit = {}
    override val onFinishButtonClick: () -> Unit = {}
    override val onSubmitButtonClick: () -> Unit = {}
}