package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.gameResults

import dev.lounres.halfhat.api.server.ServerApi
import kotlinx.coroutines.flow.StateFlow


class RealGameResultsComponent(
    override val onCopyOnlineGameKey: () -> Unit,
    override val onCopyOnlineGameLink: () -> Unit,
    override val onExitOnlineGame: () -> Unit,
    
    override val gameState: StateFlow<ServerApi.OnlineGame.State.GameResults>,
) : GameResultsComponent