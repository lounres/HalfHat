package dev.lounres.thetruehat.client.common.components.gameTimer.settings

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language


public interface SettingsPageComponent {
    public val backButtonEnabled: Boolean
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit

    public val countdownTime: Value<Int>
    public val onCountdownTimeChange: (String) -> Unit
    public val explanationTime: Value<Int>
    public val onExplanationTimeChange: (String) -> Unit
    public val finalGuessTime: Value<Int>
    public val onFinalGuessTimeChange: (String) -> Unit

    public val onStartButtonClick: () -> Unit
}