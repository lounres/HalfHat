package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundLastGuess

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


public interface RoundLastGuessComponent {
    public val gameState: StateFlow<ServerApi.OnlineGame.State.RoundLastGuess>
    
    public val onGuessed: () -> Unit
    public val onNotGuessed: () -> Unit
    public val onMistake: () -> Unit
}