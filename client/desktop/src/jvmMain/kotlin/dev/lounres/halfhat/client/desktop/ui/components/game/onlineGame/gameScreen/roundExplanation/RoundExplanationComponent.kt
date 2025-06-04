package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundExplanation

import dev.lounres.halfhat.api.server.ServerApi
import kotlinx.coroutines.flow.StateFlow


interface RoundExplanationComponent {
    val gameState: StateFlow<ServerApi.OnlineGame.State.RoundExplanation>
    
    val onGuessed: () -> Unit
    val onNotGuessed: () -> Unit
    val onMistake: () -> Unit
}