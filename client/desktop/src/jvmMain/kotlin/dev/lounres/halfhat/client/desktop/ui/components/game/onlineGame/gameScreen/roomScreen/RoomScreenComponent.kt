package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomScreen

import dev.lounres.halfhat.api.server.ServerApi
import kotlinx.coroutines.flow.StateFlow


interface RoomScreenComponent {
    val gameStateFlow: StateFlow<ServerApi.OnlineGame.State.GameInitialisation>
    
    val onOpenGameSettings: () -> Unit
    val onStartGame: () -> Unit
}