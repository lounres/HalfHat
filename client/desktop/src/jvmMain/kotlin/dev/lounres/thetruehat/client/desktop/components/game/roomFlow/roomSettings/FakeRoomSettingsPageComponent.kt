package dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomSettings

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.Settings


class FakeRoomSettingsPageComponent(
    override val backButtonEnabled: Boolean = true,
    gameEndCondition: Settings.GameEndCondition = Settings.GameEndCondition.Words,
    wordsCount: Int = 100,
    roundsCount: Int = 10,
    countdownTime: Int = 3,
    explanationTime: Int = 40,
    finalGuessTime: Int = 3,
    strictMode: Boolean = false,
) : RoomSettingsPageComponent {
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (language: Language) -> Unit = {}
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}

    override val gameEndCondition: Value<Settings.GameEndCondition> = MutableValue(gameEndCondition)
    override val onWordsGameEndConditionChoice: () -> Unit = {}
    override val onRoundsGameEndConditionChoice: () -> Unit = {}

    // TODO: Dictionary choosing

    override val wordsCount: Value<Int> = MutableValue(wordsCount)
    override val onWordsCountChange: (String) -> Unit = {}
    override val roundsCount: Value<Int> = MutableValue(roundsCount)
    override val onRoundsCountChange: (String) -> Unit = {}

    override val countdownTime: Value<Int> = MutableValue(countdownTime)
    override val onCountdownTimeChange: (String) -> Unit = {}
    override val explanationTime: Value<Int> = MutableValue(explanationTime)
    override val onExplanationTimeChange: (String) -> Unit = {}
    override val finalGuessTime: Value<Int> = MutableValue(finalGuessTime)
    override val onFinalGuessTimeChange: (String) -> Unit = {}

    override val strictMode: Value<Boolean> = MutableValue(strictMode)
    override val onStrictModeChange: (Boolean) -> Unit = {}

    override val onApplySettingsButtonClick: () -> Unit = {}
    override val onCancelButtonClick: () -> Unit = {}
}