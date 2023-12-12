package dev.lounres.thetruehat.client.desktop.components.game.roundInProgress

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language


class FakeRoundInProgressPageComponent(
    override val backButtonEnabled: Boolean = true,
    override val wordsNumber: Int = 100,
    override val showFinishButton: Boolean = true,
    override val volumeOn: Boolean = true,
    override val speakerNickname: String = "Panther",
    override val listenerNickname: String = "Jaguar",
    override val userRole: RoundInProgressPageComponent.RoundInProgressUserRole =
        RoundInProgressPageComponent.RoundInProgressUserRole.Speaker(wordToExplain = MutableValue("ДРАКОН")),
    timeLeft: Int = 138,
) : RoundInProgressPageComponent {
    override val timeLeft: Value<Int> = MutableValue(timeLeft)
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (language: Language) -> Unit = {}
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}
    override val onVolumeButtonClick: () -> Unit = {}
    override val onFinishButtonClick: () -> Unit = {}
    override val onExitButtonClick: () -> Unit = {}
}