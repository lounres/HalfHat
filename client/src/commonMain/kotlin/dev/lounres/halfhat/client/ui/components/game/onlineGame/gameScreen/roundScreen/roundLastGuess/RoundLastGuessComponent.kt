package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundLastGuess

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.hub.KoneAsynchronousHubView
import kotlinx.coroutines.flow.StateFlow


public interface RoundLastGuessComponent {
    public val gameState: StateFlow<ServerApi.OnlineGame.State.Round.LastGuess>
    
    public val darkTheme: KoneAsynchronousHubView<DarkTheme, *>
    
    public val onGuessed: () -> Unit
    public val onNotGuessed: () -> Unit
    public val onMistake: () -> Unit
}