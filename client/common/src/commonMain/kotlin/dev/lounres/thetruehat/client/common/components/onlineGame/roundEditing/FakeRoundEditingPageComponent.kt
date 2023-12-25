package dev.lounres.thetruehat.client.common.components.onlineGame.roundEditing

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState


public class FakeRoundEditingPageComponent(
    override val backButtonEnabled: Boolean = true,
    override val unitsUntilEnd: Value<UserGameState.UnitsUntilEnd> = MutableValue(UserGameState.UnitsUntilEnd.Words(100)),
    override val volumeOn: Value<Boolean> = MutableValue(true),
    override val showFinishButton: Value<Boolean> = MutableValue(true),
    override val explanationResults: List<MutableValue<UserGameState.WordExplanationResult>>? = listOf(
        MutableValue(UserGameState.WordExplanationResult(word = "реабилитация", UserGameState.WordExplanationResult.State.Explained)),
        MutableValue(UserGameState.WordExplanationResult(word = "социализация", UserGameState.WordExplanationResult.State.NotExplained)),
        MutableValue(UserGameState.WordExplanationResult(word = "кастомизация", UserGameState.WordExplanationResult.State.Mistake)),
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