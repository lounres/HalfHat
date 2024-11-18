package dev.lounres.halfhat.client.desktop.ui.components.game.timer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.lounres.halfhat.client.common.utils.DefaultSounds
import dev.lounres.halfhat.client.common.utils.playSound
import dev.lounres.halfhat.client.desktop.logic.timer.State
import dev.lounres.halfhat.client.desktop.logic.timer.timerJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CancellationException


class RealTimerPageComponent(
    componentContext: ComponentContext,
    override val onExitTimer: () -> Unit,
    volumeOn: StateFlow<Boolean>,
    initialPreparationTimeSetting: String = "3",
    initialExplanationTimeSetting: String = "40",
    initialLastGuessTimeSetting: String = "3",
): TimerPageComponent {
    val coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    
    override val timerState: MutableStateFlow<State> = MutableStateFlow(State.Finished)

    override val preparationTimeSetting: MutableStateFlow<String> = MutableStateFlow(initialPreparationTimeSetting)
    override val explanationTimeSetting: MutableStateFlow<String> = MutableStateFlow(initialExplanationTimeSetting)
    override val lastGuessTimeSetting: MutableStateFlow<String> = MutableStateFlow(initialLastGuessTimeSetting)

    var timerJob: Job? = null

    override val onStartTimer: () -> Unit = {
        runBlocking {
            timerJob?.cancelAndJoin()
        }
        timerJob = coroutineScope.timerJob(
            preparationTime = preparationTimeSetting.value.toUInt(),
            explanationTime = explanationTimeSetting.value.toUInt(),
            lastGuessTime = lastGuessTimeSetting.value.toUInt(),
        ) {
            val oldTimerState = timerState.value
            timerState.value = it

            if (volumeOn.value)
                when(it) {
                    is State.Preparation ->
                        if (oldTimerState !is State.Preparation || (oldTimerState.millisecondsLeft / 1000u) != (it.millisecondsLeft / 1000u)) playSound(DefaultSounds.preparationCountdown)
                    is State.Explanation -> if (oldTimerState is State.Preparation) playSound(DefaultSounds.explanationStart)
                    is State.LastGuess -> if (oldTimerState is State.Preparation || oldTimerState is State.Explanation) playSound(DefaultSounds.finalGuessStart)
                    State.Finished -> playSound(DefaultSounds.finalGuessEnd)
                }
        }
    }
    override val onResetTimer: () -> Unit = {
        runBlocking {
            timerJob?.cancel(CancellationException("Timer is reset"))
            timerJob?.join()
            timerState.value = State.Finished
        }
    }
}