package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.gameResults

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


public interface GameResultsComponent {
    public val gameState: StateFlow<ServerApi.OnlineGame.State.GameResults>
    
    public val onLeaveGameResults: () -> Unit
}