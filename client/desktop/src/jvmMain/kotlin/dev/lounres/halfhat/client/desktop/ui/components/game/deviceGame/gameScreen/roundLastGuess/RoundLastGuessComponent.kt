package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundLastGuess

import kotlinx.coroutines.flow.StateFlow


interface RoundLastGuessComponent {
    val onExitGame: () -> Unit
    
    val speaker: StateFlow<String>
    val listener: StateFlow<String>
    
    val millisecondsLeft: StateFlow<UInt>
    
    val word: StateFlow<String>
    
    val onGuessed: () -> Unit
    val onNotGuessed: () -> Unit
    val onMistake: () -> Unit
}