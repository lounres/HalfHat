package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.gameResults

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


class RealGameResultsComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.GameResults>,
) : GameResultsComponent