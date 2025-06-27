package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.gameResults

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


public class RealGameResultsComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.GameResults>,
) : GameResultsComponent