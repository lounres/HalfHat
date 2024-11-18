package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundLastGuess

import dev.lounres.halfhat.api.server.ServerApi
import kotlinx.coroutines.flow.StateFlow


interface RoundLastGuessComponent {
    val onCopyOnlineGameKey: () -> Unit
    val onCopyOnlineGameLink: () -> Unit
    val onExitOnlineGame: () -> Unit
    
    val gameState: StateFlow<ServerApi.OnlineGame.State.RoundLastGuess>
    
    val onGuessed: () -> Unit
    val onNotGuessed: () -> Unit
    val onMistake: () -> Unit
}