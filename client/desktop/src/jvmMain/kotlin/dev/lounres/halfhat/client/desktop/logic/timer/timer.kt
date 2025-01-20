package dev.lounres.halfhat.client.desktop.logic.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.math.max


sealed interface State {
    data object Finished: State
    data class Preparation(val millisecondsLeft: UInt): State {
        fun represent(): String = (millisecondsLeft / 1000u + 1u).toString()
    }
    data class Explanation(val millisecondsLeft: UInt): State {
        fun represent(): String {
            val secondsLeft = (millisecondsLeft / 1000u + 1u) % 60u
            val minutesLeft = millisecondsLeft / 60_000u
            return "$minutesLeft:${secondsLeft.toString().padStart(2, '0')}"
        }
    }
    data class LastGuess(val millisecondsLeft: UInt): State {
        fun represent(): String {
            val cantisecondsLeft = (millisecondsLeft / 100u + 1u) % 10u
            val secondsLeft = millisecondsLeft / 1000u
            return "$secondsLeft.${cantisecondsLeft.toString().padStart(1, '0')}"
        }
    }
}

fun CoroutineScope.timerJob(
    preparationTime: UInt,
    explanationTime: UInt,
    lastGuessTime: UInt,
    onStateUpdate: (state: State) -> Unit,
): Job {
    val preparationTimeMilliseconds = preparationTime * 1000u - 1u
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
                spentTime < preparationEndMark -> onStateUpdate(State.Preparation(preparationEndMark - spentTime))
                spentTime < explanationEndMark -> onStateUpdate(State.Explanation(explanationEndMark - spentTime))
                spentTime < lastGuessEndMark -> onStateUpdate(State.LastGuess(lastGuessEndMark - spentTime))
                else -> {
                    onStateUpdate(State.Finished)
                    break
                }
            }
            val newCurrentInstant = Clock.System.now()
            val waitTime = max(0, 100 - (newCurrentInstant - currentInstant).inWholeMilliseconds)
            delay(waitTime)
        }
    }
}