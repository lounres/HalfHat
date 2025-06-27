package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundExplanation

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


public interface RoundExplanationComponent {
    public val gameState: StateFlow<ServerApi.OnlineGame.State.RoundExplanation>
    
    public val onGuessed: () -> Unit
    public val onNotGuessed: () -> Unit
    public val onMistake: () -> Unit
}