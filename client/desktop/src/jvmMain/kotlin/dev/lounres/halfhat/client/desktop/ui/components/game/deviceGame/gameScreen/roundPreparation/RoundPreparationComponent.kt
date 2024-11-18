package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundPreparation

import kotlinx.coroutines.flow.StateFlow


interface RoundPreparationComponent{
    val onExitGame: () -> Unit
    
    val speaker: StateFlow<String>
    val listener: StateFlow<String>
    
    val millisecondsLeft: StateFlow<UInt>
}