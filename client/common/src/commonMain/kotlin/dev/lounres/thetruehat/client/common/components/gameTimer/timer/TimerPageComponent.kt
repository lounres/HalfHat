package dev.lounres.thetruehat.client.common.components.gameTimer.timer

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language


public interface TimerPageComponent {
    public val backButtonEnabled: Boolean
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit

    public val timeLeft: Value<TimerEntry>

    public val onResetButtonClick: () -> Unit

    public sealed interface TimerEntry {
        public data class Countdown(val timeLeft: Int): TimerEntry
        public data class Explanation(val timeLeft: Int): TimerEntry
        public data class FinalGuess(val timeLeft: Int): TimerEntry
    }
}