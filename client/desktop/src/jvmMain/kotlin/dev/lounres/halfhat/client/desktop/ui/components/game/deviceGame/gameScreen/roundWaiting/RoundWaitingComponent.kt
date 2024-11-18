package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundWaiting

import kotlinx.coroutines.flow.StateFlow


interface RoundWaitingComponent {
    val onExitGame: () -> Unit
    val onFinishGame: () -> Unit
    
    val speaker: StateFlow<String>
    val listener: StateFlow<String>
    
    val onStartRound: () -> Unit
}