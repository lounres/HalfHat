package dev.lounres.thetruehat.client.common.components.onlineGame.roundEditing

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState


public class RealRoundEditingPageComponent(
    public override val backButtonEnabled: Boolean,
    public override val unitsUntilEnd: Value<UserGameState.UnitsUntilEnd>,
    public override val volumeOn: Value<Boolean>,
    public override val showFinishButton: Value<Boolean>,
    public override val onBackButtonClick: () -> Unit,
    public override val onLanguageChange: (Language) -> Unit,
    public override val onFeedbackButtonClick: () -> Unit,
    public override val onHatButtonClick: () -> Unit,
    public override val onExitButtonClick: () -> Unit,
    public override val onVolumeButtonClick: () -> Unit,
    public override val onFinishButtonClick: () -> Unit,

    public override val explanationResults: List<MutableValue<UserGameState.WordExplanationResult>>?,
    public override val onSubmitButtonClick: () -> Unit,
): RoundEditingPageComponent