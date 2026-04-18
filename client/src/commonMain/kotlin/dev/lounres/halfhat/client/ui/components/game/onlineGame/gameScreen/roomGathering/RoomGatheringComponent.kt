package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roomGathering

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


public interface RoomGatheringComponent {
    public val onExitOnlineGame: () -> Unit
    public val onCopyOnlineGameKey: () -> Unit
    public val onCopyOnlineGameLink: () -> Unit
    
    public val gameState: StateFlow<ServerApi.OnlineGame.State.RoomPlayersGathering>
    
    public val onFixRoom: () -> Unit
}