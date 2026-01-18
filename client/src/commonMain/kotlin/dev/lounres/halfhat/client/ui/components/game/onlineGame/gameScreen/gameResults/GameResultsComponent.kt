package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.gameResults

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow


public interface GameResultsComponent {
    public val gameState: StateFlow<ServerApi.OnlineGame.State.GameResults>
    
    public val coroutineScope: CoroutineScope
    public val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>
    public val section: KoneMutableAsynchronousHubView<Section, *>
    
    public val onLeaveGameResults: () -> Unit
    
    public enum class Section {
        PlayersStatistic, WordsStatistic, Settings
    }
}