package dev.lounres.halfhat.client.ui.components.game.timer

import dev.lounres.halfhat.client.logic.components.game.timer.TimerState
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHub


public interface TimerPageComponent {
    public val onExitTimer: () -> Unit
    
    public val timerState: KoneAsynchronousHub<TimerState>
    
    public val preparationTimeSetting: KoneMutableAsynchronousHub<String>
    public val explanationTimeSetting: KoneMutableAsynchronousHub<String>
    public val lastGuessTimeSetting: KoneMutableAsynchronousHub<String>
    
    public val onStartTimer: () -> Unit
    public val onResetTimer: () -> Unit
}