package dev.lounres.halfhat.client.common.logic.components.game.timer

import dev.lounres.kone.hub.KoneAsynchronousHub


public interface TimerComponent {
    public val timerState: KoneAsynchronousHub<TimerState>
    
    public fun startTimer(
        preparationTimeSetting: UInt,
        explanationTimeSetting: UInt,
        lastGuessTimeSetting: UInt,
    )
    public fun resetTimer()
}