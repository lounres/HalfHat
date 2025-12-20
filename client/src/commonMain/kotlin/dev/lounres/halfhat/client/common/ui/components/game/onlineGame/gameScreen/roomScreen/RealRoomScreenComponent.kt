package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roomScreen

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


public class RealRoomScreenComponent(
    override val gameStateFlow: StateFlow<ServerApi.OnlineGame.State.GameInitialisation>,
    
    override val onOpenGameSettings: () -> Unit,
    override val onStartGame: () -> Unit
) : RoomScreenComponent