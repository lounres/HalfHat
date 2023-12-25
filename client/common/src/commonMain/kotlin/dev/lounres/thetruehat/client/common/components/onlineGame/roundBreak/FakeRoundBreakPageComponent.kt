package dev.lounres.thetruehat.client.common.components.onlineGame.roundBreak

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState


public class FakeRoundBreakPageComponent(
    override val backButtonEnabled: Boolean = true,
    unitsUntilEnd: UserGameState.UnitsUntilEnd = UserGameState.UnitsUntilEnd.Words(100),
    showFinishButton: Boolean = true,
    volumeOn: Boolean = true,
    speakerNickname: String = "Panther",
    listenerNickname: String = "Jaguar",
    override val userRole: Value<RoundBreakPageComponent.UserRole> = MutableValue(RoundBreakPageComponent.UserRole.SpeakerReady),
): RoundBreakPageComponent {
    override val unitsUntilEnd: Value<UserGameState.UnitsUntilEnd> = MutableValue(unitsUntilEnd)
    override val volumeOn: Value<Boolean> = MutableValue(volumeOn)
    override val showFinishButton: Value<Boolean> = MutableValue(showFinishButton)
    override val speakerNickname: Value<String> = MutableValue(speakerNickname)
    override val listenerNickname: Value<String> = MutableValue(listenerNickname)

    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (language: Language) -> Unit = {}
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}
    override val onExitButtonClick: () -> Unit = {}
    override val onVolumeButtonClick: () -> Unit = {}
    override val onFinishButtonClick: () -> Unit = {}
    override val onReadyButtonClick: () -> Unit = {}
}