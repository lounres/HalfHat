package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundPreparation

import dev.lounres.halfhat.api.server.ServerApi
import kotlinx.coroutines.flow.StateFlow


interface RoundPreparationComponent {
    val onCopyOnlineGameKey: () -> Unit
    val onCopyOnlineGameLink: () -> Unit
    val onExitOnlineGame: () -> Unit
    
    val gameState: StateFlow<ServerApi.OnlineGame.State.RoundPreparation>
}