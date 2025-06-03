package dev.lounres.halfhat.client.desktop.ui.components.game.timer

import dev.lounres.halfhat.client.common.utils.DefaultSounds
import dev.lounres.halfhat.client.common.utils.playSound
import dev.lounres.halfhat.client.common.utils.updateAndGet
import dev.lounres.halfhat.client.common.utils.value
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.desktop.logic.timer.State
import dev.lounres.halfhat.client.desktop.logic.timer.timerJob
import dev.lounres.komponentual.lifecycle.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicReference


class RealTimerPageComponent(
    componentContext:
    UIComponentContext,
    override val onExitTimer: () -> Unit,
    volumeOn: StateFlow<Boolean>,
    initialPreparationTimeSetting: String = "3",
    initialExplanationTimeSetting: String = "40",
    initialLastGuessTimeSetting: String = "3",
): TimerPageComponent {
    private val coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    
    override val timerState: MutableStateFlow<State> = MutableStateFlow(State.Finished)

    override val preparationTimeSetting: MutableStateFlow<String> = MutableStateFlow(initialPreparationTimeSetting)
    override val explanationTimeSetting: MutableStateFlow<String> = MutableStateFlow(initialExplanationTimeSetting)
    override val lastGuessTimeSetting: MutableStateFlow<String> = MutableStateFlow(initialLastGuessTimeSetting)

    private val timerJob = AtomicReference<Job>(Job())

    override val onStartTimer: () -> Unit = {
        coroutineScope.launch {
            timerJob.updateAndGet {
                it.cancelAndJoin()
                coroutineScope.timerJob(
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
            }.start()
        }
    }
    override val onResetTimer: () -> Unit = {
        coroutineScope.launch {
            timerJob.value.cancelAndJoin()
            timerState.value = State.Finished
        }
    }
}