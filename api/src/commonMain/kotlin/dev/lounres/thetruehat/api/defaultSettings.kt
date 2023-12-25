package dev.lounres.thetruehat.api

import dev.lounres.thetruehat.api.models.Settings


public val defaultSettings: Settings =
    Settings(
        countdownTime = 3,
        explanationTime = 40,
        finalGuessTime = 3,
        strictMode = false,
        gameEndCondition = Settings.GameEndCondition.Words,
        wordsCount = 100,
        roundsCount = 10,
        wordsSource = Settings.WordsSource.ServerDictionary(id = 0)
    )