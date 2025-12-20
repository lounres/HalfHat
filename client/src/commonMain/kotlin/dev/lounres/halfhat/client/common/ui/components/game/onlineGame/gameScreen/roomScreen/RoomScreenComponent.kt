package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roomScreen

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


public interface RoomScreenComponent {
    public val gameStateFlow: StateFlow<ServerApi.OnlineGame.State.GameInitialisation>
    
    public val onOpenGameSettings: () -> Unit
    public val onStartGame: () -> Unit
}