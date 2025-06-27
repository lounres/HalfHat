package dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundLastGuess

import kotlinx.coroutines.flow.StateFlow


public interface RoundLastGuessComponent {
    public val onExitGame: () -> Unit
    
    public val speaker: StateFlow<String>
    public val listener: StateFlow<String>
    
    public val millisecondsLeft: StateFlow<UInt>
    
    public val word: StateFlow<String>
    
    public val onGuessed: () -> Unit
    public val onNotGuessed: () -> Unit
    public val onMistake: () -> Unit
}