package dev.lounres.thetruehat.client.common.components.gameTimer.timer

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language


public class FakeTimerPageComponent(
    override val backButtonEnabled: Boolean = true,
    timeLeft: TimerPageComponent.TimerEntry = TimerPageComponent.TimerEntry.Explanation(39),
): TimerPageComponent {
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (Language) -> Unit = {}
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}

    override val timeLeft: Value<TimerPageComponent.TimerEntry> = MutableValue(timeLeft)

    override val onResetButtonClick: () -> Unit = {}
}