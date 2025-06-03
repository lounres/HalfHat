package dev.lounres.halfhat.client.common.ui.components.game.timer

import dev.lounres.halfhat.client.common.logic.components.game.timer.TimerState
import dev.lounres.kone.state.KoneMutableState
import dev.lounres.kone.state.KoneState


public interface TimerPageComponent {
    public val onExitTimer: () -> Unit
    
    public val timerState: KoneState<TimerState>
    
    public val preparationTimeSetting: KoneMutableState<UInt>
    public val explanationTimeSetting: KoneMutableState<UInt>
    public val lastGuessTimeSetting: KoneMutableState<UInt>
    
    public val onStartTimer: () -> Unit
    public val onResetTimer: () -> Unit
}