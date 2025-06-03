package dev.lounres.halfhat.client.common.logic.components.game.timer

import dev.lounres.halfhat.client.common.logic.timer.TimerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


public interface TimerComponent {
    public val timerState: StateFlow<TimerState>
    
    public val preparationTimeSetting: MutableStateFlow<UInt>
    public val explanationTimeSetting: MutableStateFlow<UInt>
    public val lastGuessTimeSetting: MutableStateFlow<UInt>
    
    public fun startTimer()
    public fun resetTimer()
}