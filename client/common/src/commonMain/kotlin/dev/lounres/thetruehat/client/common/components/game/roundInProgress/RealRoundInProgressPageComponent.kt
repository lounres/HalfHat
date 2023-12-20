package dev.lounres.thetruehat.client.common.components.game.roundInProgress

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription


public class RealRoundInProgressPageComponent(
    public override val backButtonEnabled: Boolean,
    public override val unitsUntilEnd: Value<RoomDescription.UnitsUntilEnd>,
    public override val showFinishButton: Value<Boolean>,
    public override val volumeOn: Value<Boolean>,
    public override val speakerNickname: Value<String>,
    public override val listenerNickname: Value<String>,
    public override val userRole: Value<RoundInProgressPageComponent.UserRole>,
    public override val countsUntilEnd: Value<Int>,
    public override val onBackButtonClick: () -> Unit,
    public override val onLanguageChange: (language: Language) -> Unit,
    public override val onFeedbackButtonClick: () -> Unit,
    public override val onHatButtonClick: () -> Unit,
    public override val onVolumeButtonClick: () -> Unit,
    public override val onFinishButtonClick: () -> Unit,
    public override val onExitButtonClick: () -> Unit,
    public override val onExplainedButtonClick: () -> Unit,
    public override val onNotExplainedButtonClick: () -> Unit,
    public override val onImproperlyExplainedButtonClick: () -> Unit,
): RoundInProgressPageComponent