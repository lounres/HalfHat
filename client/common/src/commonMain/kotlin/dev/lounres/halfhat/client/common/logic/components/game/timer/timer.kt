package dev.lounres.halfhat.client.common.logic.components.game.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.time.Clock


public sealed interface TimerState {
    public data object Finished: TimerState
    public data class Preparation(val millisecondsLeft: UInt): TimerState
    public data class Explanation(val millisecondsLeft: UInt): TimerState
    public data class LastGuess(val millisecondsLeft: UInt): TimerState
}

public fun TimerState.Preparation.represent(): String = millisecondsLeft.let { if (it % 1000u != 0u) it / 1000u + 1u else it / 1000u }.toString()
public fun TimerState.Explanation.represent(): String {
    val secondsLeft = millisecondsLeft.let { if (it % 1000u != 0u) it / 1000u + 1u else it / 1000u }
    val secondsToShow = secondsLeft % 60u
    val minutesToShow = secondsLeft / 60u
    return "${minutesToShow.toString().padStart(2, '0')}:${secondsToShow.toString().padStart(2, '0')}"
}
public fun TimerState.LastGuess.represent(): String {
    val decisecondsLeft = millisecondsLeft.let { if (it % 100u != 0u) it / 100u + 1u else it / 100u }
    val decisecondsToShow = decisecondsLeft % 10u
    val secondsToShow = decisecondsLeft / 10u
    return "$secondsToShow.$decisecondsToShow"
}

public fun CoroutineScope.timerJob(
    preparationTime: UInt,
    explanationTime: UInt,
    lastGuessTime: UInt,
    onStateUpdate: suspend (state: TimerState) -> Unit,
): Job {
    val preparationTimeMilliseconds = preparationTime * 1000u
    val explanationTimeMilliseconds = explanationTime * 1000u
    val lastGuessTimeMilliseconds = lastGuessTime * 1000u

    val preparationEndMark = preparationTimeMilliseconds
    val explanationEndMark = preparationEndMark + explanationTimeMilliseconds
    val lastGuessEndMark = explanationEndMark + lastGuessTimeMilliseconds

    val startInstant = Clock.System.now()
    return launch {
        while (true) {
            val currentInstant = Clock.System.now()
            val spentTime = (currentInstant - startInstant).inWholeMilliseconds.toUInt()
            when {
                spentTime < preparationEndMark -> onStateUpdate(TimerState.Preparation(preparationEndMark - spentTime))
                spentTime < explanationEndMark -> onStateUpdate(TimerState.Explanation(explanationEndMark - spentTime))
                spentTime < lastGuessEndMark -> onStateUpdate(TimerState.LastGuess(lastGuessEndMark - spentTime))
                else -> {
                    onStateUpdate(TimerState.Finished)
                    break
                }
            }
            val newCurrentInstant = Clock.System.now()
            val waitTime = max(0, 100 - (newCurrentInstant - currentInstant).inWholeMilliseconds)
            delay(waitTime)
        }
    }
}