package dev.lounres.halfhat.client.common.logic.components.game.timer

import dev.lounres.halfhat.client.common.logic.timer.TimerState
import dev.lounres.halfhat.client.common.logic.timer.timerJob
import dev.lounres.halfhat.client.common.utils.DefaultSounds
import dev.lounres.halfhat.client.common.utils.playSound
import dev.lounres.halfhat.client.components.LogicComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


public class RealTimerComponent(
    componentContext: LogicComponentContext,
    public val volumeOn: StateFlow<Boolean>,
    initialPreparationTimeSetting: UInt = 3u,
    initialExplanationTimeSetting: UInt = 40u,
    initialLastGuessTimeSetting: UInt = 3u,
) : TimerComponent {
    private val coroutineScope: CoroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    
    override val timerState: MutableStateFlow<TimerState> = MutableStateFlow(TimerState.Finished)
    
    override val preparationTimeSetting: MutableStateFlow<UInt> = MutableStateFlow(initialPreparationTimeSetting)
    override val explanationTimeSetting: MutableStateFlow<UInt> = MutableStateFlow(initialExplanationTimeSetting)
    override val lastGuessTimeSetting: MutableStateFlow<UInt> = MutableStateFlow(initialLastGuessTimeSetting)
    
    private var timerJob: Job? = null
    
    override fun startTimer() {
        coroutineScope.launch {
            timerJob?.cancelAndJoin()
            timerJob = coroutineScope.timerJob(
                preparationTime = preparationTimeSetting.value,
                explanationTime = explanationTimeSetting.value,
                lastGuessTime = lastGuessTimeSetting.value,
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
        }
    }
    override fun resetTimer() {
        coroutineScope.launch {
            timerJob?.cancel()
            timerJob?.join()
            timerState.value = TimerState.Finished
        }
    }
}