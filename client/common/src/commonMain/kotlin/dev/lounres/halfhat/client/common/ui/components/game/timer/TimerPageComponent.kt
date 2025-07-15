package dev.lounres.halfhat.client.common.ui.components.game.timer

import dev.lounres.halfhat.client.common.logic.components.game.timer.TimerState
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.KoneMutableBlockingHub


public interface TimerPageComponent {
    public val onExitTimer: () -> Unit
    
    public val timerState: KoneAsynchronousHub<TimerState>
    
    public val preparationTimeSetting: KoneMutableBlockingHub<String>
    public val explanationTimeSetting: KoneMutableBlockingHub<String>
    public val lastGuessTimeSetting: KoneMutableBlockingHub<String>
    
    public val onStartTimer: () -> Unit
    public val onResetTimer: () -> Unit
}