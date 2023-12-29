package dev.lounres.thetruehat.client.common.components.gameTimer.settings

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import dev.lounres.thetruehat.api.localization.Language


public class RealSettingsPageComponent(
    override val backButtonEnabled: Boolean,
    override val onBackButtonClick: () -> Unit,
    override val onLanguageChange: (Language) -> Unit,
    override val onFeedbackButtonClick: () -> Unit,
    override val onHatButtonClick: () -> Unit,
    public val onStart: (countdownTime: Int, explanationTime: Int, finalGuessTime: Int) -> Unit,
) : SettingsPageComponent {
    override val countdownTime: MutableValue<Int> = MutableValue(3)
    override val onCountdownTimeChange: (String) -> Unit = { input ->
        input.toIntOrNull()?.let { value -> countdownTime.update { value } }
    }
    override val explanationTime: MutableValue<Int> = MutableValue(40)
    override val onExplanationTimeChange: (String) -> Unit = { input ->
        input.toIntOrNull()?.let { value -> explanationTime.update { value } }
    }
    override val finalGuessTime: MutableValue<Int> = MutableValue(3)
    override val onFinalGuessTimeChange: (String) -> Unit = { input ->
        input.toIntOrNull()?.let { value -> finalGuessTime.update { value } }
    }

    override val onStartButtonClick: () -> Unit = { onStart(countdownTime.value, explanationTime.value, finalGuessTime.value) }
}