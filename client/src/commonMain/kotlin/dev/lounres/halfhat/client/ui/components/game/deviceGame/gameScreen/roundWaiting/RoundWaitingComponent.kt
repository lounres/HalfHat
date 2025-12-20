package dev.lounres.halfhat.client.ui.components.game.deviceGame.gameScreen.roundWaiting

import kotlinx.coroutines.flow.StateFlow


public interface RoundWaitingComponent {
    public val onExitGame: () -> Unit
    public val onFinishGame: () -> Unit
    
    public val speaker: StateFlow<String>
    public val listener: StateFlow<String>
    
    public val onStartRound: () -> Unit
}