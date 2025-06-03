package dev.lounres.halfhat.client.common.logic.components.game.timer

import dev.lounres.kone.state.KoneMutableState
import dev.lounres.kone.state.KoneState


public interface TimerComponent {
    public val timerState: KoneState<TimerState>
    
    public fun startTimer(
        preparationTimeSetting: UInt,
        explanationTimeSetting: UInt,
        lastGuessTimeSetting: UInt,
    )
    public fun resetTimer()
}