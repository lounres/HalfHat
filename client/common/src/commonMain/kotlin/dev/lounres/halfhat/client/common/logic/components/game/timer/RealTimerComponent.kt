package dev.lounres.halfhat.client.common.logic.components.game.timer

import dev.lounres.halfhat.client.common.utils.DefaultSounds
import dev.lounres.halfhat.client.common.utils.playSound
import dev.lounres.halfhat.client.components.LogicComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.lifecycle
import dev.lounres.komponentual.lifecycle.LogicComponentLifecycleState
import dev.lounres.kone.state.KoneMutableState
import dev.lounres.utils.kotlinConcurrentAtomics.updateAndGet
import dev.lounres.utils.kotlinConcurrentAtomics.value
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
            coroutineScope.launch {
                timerJob.updateAndGet {
                    it.cancel()
                    coroutineScope.timerJob(
                        preparationTime = preparationTimeSetting,
                        explanationTime = explanationTimeSetting,
                        lastGuessTime = lastGuessTimeSetting,
                    ) {
                        val oldTimerState = timerState.value
                        timerState.value = it
                        
                        if (volumeOn.value)
                            when(it) {
                                is TimerState.Preparation ->
                                    if (oldTimerState !is TimerState.Preparation || (oldTimerState.millisecondsLeft / 1000u) != (it.millisecondsLeft / 1000u))
                                        playSound(DefaultSounds.preparationCountdown)
                                is TimerState.Explanation ->
                                    if (oldTimerState is TimerState.Preparation)
                                        playSound(DefaultSounds.explanationStart)
                                is TimerState.LastGuess ->
                                    if (oldTimerState is TimerState.Preparation || oldTimerState is TimerState.Explanation)
                                        playSound(DefaultSounds.finalGuessStart)
                                TimerState.Finished -> playSound(DefaultSounds.finalGuessEnd)
                            }
                    }
                }.start()
            }
    }
    override fun resetTimer() {
        coroutineScope.launch {
            timerJob.value.cancelAndJoin()
            timerState.value = TimerState.Finished
        }
    }
}