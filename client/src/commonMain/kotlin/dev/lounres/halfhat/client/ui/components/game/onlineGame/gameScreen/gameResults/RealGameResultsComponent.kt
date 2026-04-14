package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.gameResults

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow


public class RealGameResultsComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.GameResults>,
    
    override val coroutineScope: CoroutineScope,
    override val darkTheme: KoneMutableAsynchronousHub<DarkTheme>,
    
    override val onLeaveGameResults: () -> Unit,
) : GameResultsComponent {
    override val section: KoneMutableAsynchronousHub<GameResultsComponent.Section> =
        KoneMutableAsynchronousHub(GameResultsComponent.Section.PlayersStatistic)
}