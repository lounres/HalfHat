package dev.lounres.halfhat.client.ui.components.game.controller.gameScreen

import dev.lounres.halfhat.client.logic.components.game.timer.TimerState
import dev.lounres.kone.hub.KoneAsynchronousHub


public interface GameScreenComponent {
    public val onExitGameController: () -> Unit
    
    public val timerState: KoneAsynchronousHub<TimerState>
    public val speaker: KoneAsynchronousHub<String>
    public val listener: KoneAsynchronousHub<String>
    
    public val onStartTimer: () -> Unit
    public val onFinishTimer: () -> Unit
}