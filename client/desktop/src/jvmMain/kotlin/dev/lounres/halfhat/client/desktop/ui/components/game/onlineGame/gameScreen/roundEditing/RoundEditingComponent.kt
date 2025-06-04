package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundEditing

import dev.lounres.halfhat.api.server.ServerApi
import kotlinx.coroutines.flow.StateFlow


interface RoundEditingComponent {
    val gameState: StateFlow<ServerApi.OnlineGame.State.RoundEditing>
    
    val onGuessed: (UInt) -> Unit
    val onNotGuessed: (UInt) -> Unit
    val onMistake: (UInt) -> Unit
    
    val onConfirm: () -> Unit
}