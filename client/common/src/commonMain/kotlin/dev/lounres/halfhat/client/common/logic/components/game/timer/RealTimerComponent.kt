package dev.lounres.halfhat.client.common.logic.components.game.timer

import dev.lounres.halfhat.client.common.utils.DefaultSounds
import dev.lounres.halfhat.client.common.utils.play
import dev.lounres.halfhat.client.components.LogicComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.lifecycle.LogicComponentLifecycleState
import dev.lounres.halfhat.client.components.lifecycle.lifecycle
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.set
import dev.lounres.kone.relations.defaultEquality
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


public class RealTimerComponent(
    private val componentContext: LogicComponentContext,
    public val volumeOn: StateFlow<Boolean>,
) : TimerComponent {
    private val coroutineScope: CoroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    
    override val timerState: KoneMutableAsynchronousHub<TimerState> = KoneMutableAsynchronousHub(TimerState.Finished, elementEquality = defaultEquality() /* FIXME: Remove the default value */)
    
    private val timerJobLock = ReentrantLock()
    private var timerJob: Job = Job()
    
    override fun startTimer(
        preparationTimeSetting: UInt,
        explanationTimeSetting: UInt,
        lastGuessTimeSetting: UInt,
    ) {
        if (componentContext.lifecycle.state == LogicComponentLifecycleState.Running)
            timerJobLock.withLock {
                timerJob.cancel()
                coroutineScope.timerJob(
                    preparationTime = preparationTimeSetting,
                    explanationTime = explanationTimeSetting,
                    lastGuessTime = lastGuessTimeSetting,
                ) { newTimerState ->
                    val oldTimerState = timerState.value
                    timerState.set(newTimerState)
                    
                    if (volumeOn.value)
                        when(newTimerState) {
                            is TimerState.Preparation ->
                                if (oldTimerState !is TimerState.Preparation || oldTimerState.millisecondsLeft.let { if (it % 1000u != 0u) it / 1000u + 1u else it / 1000u } != newTimerState.millisecondsLeft.let { if (it % 1000u != 0u) it / 1000u + 1u else it / 1000u })
                                    coroutineScope.launch { DefaultSounds.preparationCountdown.await().play() }
                            is TimerState.Explanation ->
                                if (oldTimerState !is TimerState.Explanation)
                                    coroutineScope.launch { DefaultSounds.explanationStart.await().play() }
                            is TimerState.LastGuess ->
                                if (oldTimerState !is TimerState.LastGuess)
                                    coroutineScope.launch { DefaultSounds.finalGuessStart.await().play() }
                            TimerState.Finished ->
                                coroutineScope.launch { DefaultSounds.finalGuessEnd.await().play() }
                        }
                }.also { timerJob = it }
            }.start()
    }
    override fun resetTimer() {
        coroutineScope.launch {
            timerJob.cancelAndJoin()
            timerState.set(TimerState.Finished)
        }
    }
}