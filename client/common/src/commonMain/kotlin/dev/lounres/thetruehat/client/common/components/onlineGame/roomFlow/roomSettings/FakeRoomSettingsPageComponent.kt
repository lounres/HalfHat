package dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow.roomSettings

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.Settings
import dev.lounres.thetruehat.api.models.UserGameState


public class FakeRoomSettingsPageComponent(
    override val backButtonEnabled: Boolean = true,
    gameEndCondition: Settings.GameEndCondition = Settings.GameEndCondition.Words,
    wordsCount: Int = 100,
    roundsCount: Int = 10,
    countdownTime: Int = 3,
    explanationTime: Int = 40,
    finalGuessTime: Int = 3,
    strictMode: Boolean = false,
    dictionary: UserGameState.ServerDictionary = UserGameState.ServerDictionary(id = 0, name = "Тестовый словарь", wordsCount = 57),
    availableDictionaries: List<UserGameState.ServerDictionary> = listOf(
        UserGameState.ServerDictionary(id = 0, name = "Тестовый словарь", wordsCount = 57),
        UserGameState.ServerDictionary(id = 1, name = "Тестовый словарь 2", wordsCount = 179),
        UserGameState.ServerDictionary(id = 0, name = "Тестовый словарь 3", wordsCount = 2)
    ),
) : RoomSettingsPageComponent {
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (language: Language) -> Unit = {}
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}

    override val gameEndCondition: Value<Settings.GameEndCondition> = MutableValue(gameEndCondition)
    override val onWordsGameEndConditionChoice: () -> Unit = {}
    override val onRoundsGameEndConditionChoice: () -> Unit = {}

    override val dictionary: Value<UserGameState.ServerDictionary> = MutableValue(dictionary)
    override val availableDictionaries: Value<List<UserGameState.ServerDictionary>> = MutableValue(availableDictionaries)
    override val onDictionaryChange: (Settings.WordsSource) -> Unit = {}

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