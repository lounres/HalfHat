package dev.lounres.thetruehat.client.common.components.onlineGame.roundEditing

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState


public interface RoundEditingPageComponent {
    public val backButtonEnabled: Boolean
    public val unitsUntilEnd: Value<UserGameState.UnitsUntilEnd>
    public val volumeOn: Value<Boolean>
    public val showFinishButton: Value<Boolean>
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit
    public val onExitButtonClick: () -> Unit
    public val onVolumeButtonClick: () -> Unit
    public val onFinishButtonClick: () -> Unit

    public val explanationResults: List<MutableValue<UserGameState.WordExplanationResult>>?
    public val onSubmitButtonClick: () -> Unit
}