package dev.lounres.halfhat.client.common.logic.components.game.timer

import dev.lounres.halfhat.client.common.utils.DefaultSounds
import dev.lounres.halfhat.client.common.utils.play
import dev.lounres.halfhat.client.components.LogicComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.lifecycle.LogicComponentLifecycleState
import dev.lounres.halfhat.client.components.lifecycle.lifecycle
import dev.lounres.kone.state.KoneMutableState
import dev.lounres.util.atomic.updateAndGet
import dev.lounres.util.atomic.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicReference


public class RealTimerComponent(
    private val componentContext: LogicComponentContext,
    public val volumeOn: StateFlow<Boolean>,
) : TimerComponent {
    private val coroutineScope: CoroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    
    override val timerState: KoneMutableState<TimerState> = KoneMutableState(TimerState.Finished)
    
    private val timerJob = AtomicReference<Job>(Job())
    
    override fun startTimer(
        preparationTimeSetting: UInt,
        explanationTimeSetting: UInt,
        lastGuessTimeSetting: UInt,
    ) {
        if (componentContext.lifecycle.state == LogicComponentLifecycleState.Running)
            timerJob.updateAndGet {
                it.cancel()
                coroutineScope.timerJob(
                    preparationTime = preparationTimeSetting,
                    explanationTime = explanationTimeSetting,
                    lastGuessTime = lastGuessTimeSetting,
                ) { newTimerState ->
                    val oldTimerState = timerState.value
                    timerState.value = newTimerState
                    
                    if (volumeOn.value)
                        when(newTimerState) {
                            is TimerState.Preparation ->
                                if (volumeOn.value && (oldTimerState !is TimerState.Preparation || oldTimerState.millisecondsLeft.let { if (it % 1000u != 0u) it / 1000u + 1u else it / 1000u } != newTimerState.millisecondsLeft.let { if (it % 1000u != 0u) it / 1000u + 1u else it / 1000u }))
                                    coroutineScope.launch { DefaultSounds.preparationCountdown.await().play() }
                            is TimerState.Explanation ->
                                if (volumeOn.value && oldTimerState !is TimerState.Explanation)
                                    coroutineScope.launch { DefaultSounds.explanationStart.await().play() }
                            is TimerState.LastGuess ->
                                if (volumeOn.value && oldTimerState !is TimerState.LastGuess)
                                    coroutineScope.launch { DefaultSounds.finalGuessStart.await().play() }
                            TimerState.Finished ->
                                if (volumeOn.value)
                                    coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                        }
                }
            }.start()
    }
    override fun resetTimer() {
        coroutineScope.launch {
            timerJob.value.cancelAndJoin()
            timerState.value = TimerState.Finished
        }
    }
}