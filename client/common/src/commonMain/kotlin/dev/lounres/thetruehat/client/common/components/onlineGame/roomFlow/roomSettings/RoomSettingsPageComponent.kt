package dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow.roomSettings

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.Settings
import dev.lounres.thetruehat.api.models.UserGameState


public interface RoomSettingsPageComponent {
    public val backButtonEnabled: Boolean
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (language: Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit

    public val gameEndCondition: Value<Settings.GameEndCondition>
    public val onWordsGameEndConditionChoice: () -> Unit
    public val onRoundsGameEndConditionChoice: () -> Unit

    // TODO: Dictionary choosing
    public val dictionary: Value<UserGameState.ServerDictionary>
    public val availableDictionaries: Value<List<UserGameState.ServerDictionary>>
    public val onDictionaryChange: (Settings.WordsSource) -> Unit

    public val wordsCount: Value<Int>
    public val onWordsCountChange: (String) -> Unit
    public val roundsCount: Value<Int>
    public val onRoundsCountChange: (String) -> Unit

    public val countdownTime: Value<Int>
    public val onCountdownTimeChange: (String) -> Unit
    public val explanationTime: Value<Int>
    public val onExplanationTimeChange: (String) -> Unit
    public val finalGuessTime: Value<Int>
    public val onFinalGuessTimeChange: (String) -> Unit

    public val strictMode: Value<Boolean>
    public val onStrictModeChange: (Boolean) -> Unit

    public val onApplySettingsButtonClick: () -> Unit
    public val onCancelButtonClick: () -> Unit
}