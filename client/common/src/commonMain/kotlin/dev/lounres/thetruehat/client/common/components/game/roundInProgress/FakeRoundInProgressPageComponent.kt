package dev.lounres.thetruehat.client.common.components.game.roundInProgress

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription


public class FakeRoundInProgressPageComponent(
    override val backButtonEnabled: Boolean = true,
    override val unitsUntilEnd: Value<RoomDescription.UnitsUntilEnd> = MutableValue(RoomDescription.UnitsUntilEnd.Words(100)),
    override val showFinishButton: Value<Boolean> = MutableValue(true),
    override val volumeOn: Value<Boolean> = MutableValue(true),
    override val speakerNickname: Value<String> = MutableValue("Panther"),
    override val listenerNickname: Value<String> = MutableValue("Jaguar"),
    override val userRole: Value<RoundInProgressPageComponent.UserRole> =
        MutableValue(RoundInProgressPageComponent.UserRole.Speaker(wordToExplain = MutableValue("ДРАКОН"))),
    timeLeft: Int = 138,
) : RoundInProgressPageComponent {
    override val countsUntilEnd: Value<Int> = MutableValue(timeLeft)
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (language: Language) -> Unit = {}
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}
    override val onVolumeButtonClick: () -> Unit = {}
    override val onFinishButtonClick: () -> Unit = {}
    override val onExitButtonClick: () -> Unit = {}

    override val onExplainedButtonClick: () -> Unit = {}
    override val onNotExplainedButtonClick: () -> Unit = {}
    override val onImproperlyExplainedButtonClick: () -> Unit = {}
}