package dev.lounres.thetruehat.client.common.components.gameTimer.settings

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language


public class FakeSettingsPageComponent(
    override val backButtonEnabled: Boolean = true,
    countdownTime: Int = 3,
    explanationTime: Int = 40,
    finalGuessTime: Int = 3,
): SettingsPageComponent {
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (Language) -> Unit = {}
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}

    override val countdownTime: Value<Int> = MutableValue(countdownTime)
    override val onCountdownTimeChange: (String) -> Unit = {}
    override val explanationTime: Value<Int> = MutableValue(explanationTime)
    override val onExplanationTimeChange: (String) -> Unit = {}
    override val finalGuessTime: Value<Int> = MutableValue(finalGuessTime)
    override val onFinalGuessTimeChange: (String) -> Unit = {}

    override val onStartButtonClick: () -> Unit = {}
}