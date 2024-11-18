package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomScreen

import dev.lounres.halfhat.api.server.ServerApi
import kotlinx.coroutines.flow.StateFlow


interface RoomScreenComponent {
    val onCopyOnlineGameKey: () -> Unit
    val onCopyOnlineGameLink: () -> Unit
    val onExitOnlineGame: () -> Unit
    
    val gameStateFlow: StateFlow<ServerApi.OnlineGame.State.GameInitialisation>
    
    val onOpenGameSettings: () -> Unit
    val onStartGame: () -> Unit
}