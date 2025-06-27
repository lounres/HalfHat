package dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundPreparation

import kotlinx.coroutines.flow.StateFlow


public interface RoundPreparationComponent{
    public val onExitGame: () -> Unit
    
    public val speaker: StateFlow<String>
    public val listener: StateFlow<String>
    
    public val millisecondsLeft: StateFlow<UInt>
}