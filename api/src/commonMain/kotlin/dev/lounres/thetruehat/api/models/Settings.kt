package dev.lounres.thetruehat.api.models

import kotlinx.serialization.Serializable


@Serializable
public data class Settings(
    val countdownTime: Int,
    val explanationTime: Int,
    val finalGuessTime: Int,
    val strictMode: Boolean,
    val gameEndCondition: GameEndCondition,
    val wordsCount: Int,
    val roundsCount: Int,
    val wordsSource: WordsSource,
) {
    @Serializable
    public enum class GameEndCondition {
        Words, Rounds;
    }
    @Serializable
    public sealed interface WordsSource {
        @Serializable
        public data class ServerDictionary(
            val dictionaryId: Int,
        ): WordsSource
        // TODO
    }
}

@Serializable
public data class SettingsUpdate(
    val countdownTime: Int? = null,
    val explanationTime: Int? = null,
    val finalGuessTime: Int? = null,
    val strictMode: Boolean? = null,
    val gameEndCondition: Settings.GameEndCondition? = null,
    val roundsCount: Int? = null,
    val wordsCount: Int? = null,
    val wordsSource: Settings.WordsSource? = null,
)

public fun Settings.updateWith(update: SettingsUpdate): Settings =
    Settings(
        countdownTime = update.countdownTime ?: this.countdownTime,
        explanationTime = update.explanationTime ?: this.explanationTime,
        finalGuessTime = update.finalGuessTime ?: this.finalGuessTime,
        strictMode = update.strictMode ?: this.strictMode,
        gameEndCondition = update.gameEndCondition ?: this.gameEndCondition,
        wordsCount = update.wordsCount ?: this.wordsCount,
        roundsCount = update.roundsCount ?: this.roundsCount,
        wordsSource = update.wordsSource ?: this.wordsSource,
    )