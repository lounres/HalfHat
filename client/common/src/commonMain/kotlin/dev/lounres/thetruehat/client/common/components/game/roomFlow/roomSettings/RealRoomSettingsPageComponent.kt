package dev.lounres.thetruehat.client.common.components.game.roomFlow.roomSettings

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.decompose.value.update
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.Settings
import dev.lounres.thetruehat.api.models.SettingsUpdate


public class RealRoomSettingsPageComponent(
    override val backButtonEnabled: Boolean,
    override val onBackButtonClick: () -> Unit,
    override val onLanguageChange: (language: Language) -> Unit,
    override val onFeedbackButtonClick: () -> Unit,
    override val onHatButtonClick: () -> Unit,

    public val settings: Settings,
    public val onApplySettings: (SettingsUpdate) -> Unit,
    override val onCancelButtonClick: () -> Unit
): RoomSettingsPageComponent {
    public val settingsUpdate: MutableValue<SettingsUpdate> = MutableValue(SettingsUpdate())

    override val gameEndCondition: Value<Settings.GameEndCondition> = settingsUpdate.map { it.gameEndCondition ?: settings.gameEndCondition }
    override val onWordsGameEndConditionChoice: () -> Unit = { settingsUpdate.update { it.copy(gameEndCondition = Settings.GameEndCondition.Words) } }
    override val onRoundsGameEndConditionChoice: () -> Unit = { settingsUpdate.update { it.copy(gameEndCondition = Settings.GameEndCondition.Rounds) } }

    // TODO: Dictionary choosing

    override val wordsCount: Value<Int> = settingsUpdate.map { it.wordsCount ?: settings.wordsCount }
    override val onWordsCountChange: (String) -> Unit = { input ->
        input.toIntOrNull()?.let { value -> settingsUpdate.update { it.copy(wordsCount = value) } }
    }
    override val roundsCount: Value<Int> = settingsUpdate.map { it.roundsCount ?: settings.roundsCount }
    override val onRoundsCountChange: (String) -> Unit = { input ->
        input.toIntOrNull()?.let { value -> settingsUpdate.update { it.copy(roundsCount = value) } }
    }

    override val countdownTime: Value<Int> = settingsUpdate.map { it.countdownTime ?: settings.countdownTime }
    override val onCountdownTimeChange: (String) -> Unit = { input ->
        input.toIntOrNull()?.let { value -> settingsUpdate.update { it.copy(countdownTime = value) } }
    }
    override val explanationTime: Value<Int> = settingsUpdate.map { it.explanationTime ?: settings.explanationTime }
    override val onExplanationTimeChange: (String) -> Unit = { input ->
        input.toIntOrNull()?.let { value -> settingsUpdate.update { it.copy(explanationTime = value) } }
    }
    override val finalGuessTime: Value<Int> = settingsUpdate.map { it.finalGuessTime ?: settings.finalGuessTime }
    override val onFinalGuessTimeChange: (String) -> Unit = { input ->
        input.toIntOrNull()?.let { value -> settingsUpdate.update { it.copy(finalGuessTime = value) } }
    }

    override val strictMode: Value<Boolean> = settingsUpdate.map { it.strictMode ?: settings.strictMode }
    override val onStrictModeChange: (Boolean) -> Unit = { input ->
        settingsUpdate.update { it.copy(strictMode = input) }
    }

    override val onApplySettingsButtonClick: () -> Unit = { onApplySettings(settingsUpdate.value) }
}