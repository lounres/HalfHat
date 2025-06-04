package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomScreen

import dev.lounres.halfhat.api.server.ServerApi
import kotlinx.coroutines.flow.StateFlow


class RealRoomScreenComponent(
    override val gameStateFlow: StateFlow<ServerApi.OnlineGame.State.GameInitialisation>,
    
    override val onOpenGameSettings: () -> Unit,
    override val onStartGame: () -> Unit
) : RoomScreenComponent