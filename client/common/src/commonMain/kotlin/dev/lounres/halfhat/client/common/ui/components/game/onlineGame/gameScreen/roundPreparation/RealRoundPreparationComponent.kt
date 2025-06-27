package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundPreparation

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


public class RealRoundPreparationComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundPreparation>
) : RoundPreparationComponent