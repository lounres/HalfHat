package dev.lounres.thetruehat.client.common.components.onlineGame.roundBreak

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState


public class RealRoundBreakPageComponent(
    override val backButtonEnabled: Boolean,
    override val unitsUntilEnd: Value<UserGameState.UnitsUntilEnd>,
    override val volumeOn: Value<Boolean>,
    override val showFinishButton: Value<Boolean>,
    override val speakerNickname: Value<String>,
    override val listenerNickname: Value<String>,
    override val userRole: Value<RoundBreakPageComponent.UserRole>,
    override val onBackButtonClick: () -> Unit,
    override val onLanguageChange: (language: Language) -> Unit,
    override val onFeedbackButtonClick: () -> Unit,
    override val onHatButtonClick: () -> Unit,
    override val onExitButtonClick: () -> Unit,
    override val onVolumeButtonClick: () -> Unit,
    override val onFinishButtonClick: () -> Unit,
    override val onReadyButtonClick: () -> Unit,
): RoundBreakPageComponent {
    init {
        showFinishButton.subscribe { println("!!!$it") }
    }
}