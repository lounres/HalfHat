package dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomSettings

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.Settings


interface RoomSettingsPageComponent {
    val backButtonEnabled: Boolean
    val onBackButtonClick: () -> Unit
    val onLanguageChange: (language: Language) -> Unit
    val onFeedbackButtonClick: () -> Unit
    val onHatButtonClick: () -> Unit

    val gameEndCondition: Value<Settings.GameEndCondition>
    val onWordsGameEndConditionChoice: () -> Unit
    val onRoundsGameEndConditionChoice: () -> Unit

    // TODO: Dictionary choosing

    val wordsCount: Value<Int>
    val onWordsCountChange: (String) -> Unit
    val roundsCount: Value<Int>
    val onRoundsCountChange: (String) -> Unit

    val countdownTime: Value<Int>
    val onCountdownTimeChange: (String) -> Unit
    val explanationTime: Value<Int>
    val onExplanationTimeChange: (String) -> Unit
    val finalGuessTime: Value<Int>
    val onFinalGuessTimeChange: (String) -> Unit

    val strictMode: Value<Boolean>
    val onStrictModeChange: (Boolean) -> Unit

    val onApplySettingsButtonClick: () -> Unit
    val onCancelButtonClick: () -> Unit
}