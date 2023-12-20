package dev.lounres.thetruehat.client.common.components.game.roundCountdown

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription


public class RealRoundCountdownPageComponent(
    override val backButtonEnabled: Boolean,
    override val unitsUntilEnd: Value<RoomDescription.UnitsUntilEnd>,
    override val volumeOn: Value<Boolean>,
    override val showFinishButton: Value<Boolean>,
    override val countsUntilStart: Value<Int>,
    override val onBackButtonClick: () -> Unit,
    override val onLanguageChange: (Language) -> Unit,
    override val onFeedbackButtonClick: () -> Unit,
    override val onHatButtonClick: () -> Unit,
    override val onExitButtonClick: () -> Unit,
    override val onVolumeButtonClick: () -> Unit,
    override val onFinishButtonClick: () -> Unit,
): RoundCountdownPageComponent