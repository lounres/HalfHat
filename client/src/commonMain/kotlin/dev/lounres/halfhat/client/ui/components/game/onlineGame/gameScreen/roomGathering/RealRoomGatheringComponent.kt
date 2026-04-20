package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roomGathering

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


public class RealRoomGatheringComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoomPlayersGathering>,
    
    override val onExitOnlineGame: () -> Unit,
    override val onCopyOnlineGameKey: () -> Unit,
    override val onCopyOnlineGameLink: () -> Unit,
    
    override val onFixRoom: () -> Unit,
) : RoomGatheringComponent