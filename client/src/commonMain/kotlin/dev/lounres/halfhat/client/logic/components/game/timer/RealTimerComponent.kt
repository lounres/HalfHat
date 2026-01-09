package dev.lounres.halfhat.client.logic.components.game.timer

import dev.lounres.halfhat.client.components.LogicComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.lifecycle.LogicComponentLifecycleState
import dev.lounres.halfhat.client.components.lifecycle.lifecycle
import dev.lounres.halfhat.client.logic.settings.playExplanationStart
import dev.lounres.halfhat.client.logic.settings.playFinalGuessEnd
import dev.lounres.halfhat.client.logic.settings.playFinalGuessStart
import dev.lounres.halfhat.client.logic.settings.playPreparationCountdown
import dev.lounres.halfhat.client.logic.settings.volumeOn
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.set
import dev.lounres.kone.hub.value
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch


public class RealTimerComponent(
    private val componentContext: LogicComponentContext,
) : TimerComponent {
    private val settings = componentContext.settings
    private val coroutineScope: CoroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    
    override val timerState: KoneMutableAsynchronousHub<TimerState> = KoneMutableAsynchronousHub(TimerState.Finished)
    
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
                    
                    if (componentContext.settings.value.volumeOn)
                        when(newTimerState) {
                            is TimerState.Preparation ->
                                if (oldTimerState !is TimerState.Preparation || oldTimerState.millisecondsLeft.let { if (it % 1000u != 0u) it / 1000u + 1u else it / 1000u } != newTimerState.millisecondsLeft.let { if (it % 1000u != 0u) it / 1000u + 1u else it / 1000u })
                                    coroutineScope.launch { settings.playPreparationCountdown() }
                            is TimerState.Explanation ->
                                if (oldTimerState !is TimerState.Explanation)
                                    coroutineScope.launch { settings.playExplanationStart() }
                            is TimerState.LastGuess ->
                                if (oldTimerState !is TimerState.LastGuess)
                                    coroutineScope.launch { settings.playFinalGuessStart() }
                            TimerState.Finished ->
                                coroutineScope.launch { settings.playFinalGuessEnd() }
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