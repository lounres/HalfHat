package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundPreparation

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


public interface RoundPreparationComponent {
    public val gameState: StateFlow<ServerApi.OnlineGame.State.Round.Preparation>
}