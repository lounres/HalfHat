package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundExplanation

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.hub.KoneAsynchronousHubView
import kotlinx.coroutines.flow.StateFlow


public interface RoundExplanationComponent {
    public val gameState: StateFlow<ServerApi.OnlineGame.State.Round.Explanation>
    
    public val darkTheme: KoneAsynchronousHubView<DarkTheme, *>
    public val onGuessed: () -> Unit
    public val onNotGuessed: () -> Unit
    public val onMistake: () -> Unit
}