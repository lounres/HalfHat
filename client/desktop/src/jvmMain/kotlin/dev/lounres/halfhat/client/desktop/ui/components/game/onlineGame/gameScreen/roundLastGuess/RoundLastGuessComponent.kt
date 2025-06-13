package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundLastGuess

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


interface RoundLastGuessComponent {
    val gameState: StateFlow<ServerApi.OnlineGame.State.RoundLastGuess>
    
    val onGuessed: () -> Unit
    val onNotGuessed: () -> Unit
    val onMistake: () -> Unit
}