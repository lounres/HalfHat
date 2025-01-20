package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.gameResults

import dev.lounres.halfhat.api.server.ServerApi
import dev.lounres.kone.collections.list.emptyKoneList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeGameResultsComponent(
    initialGameState: ServerApi.OnlineGame.State.GameResults =
        ServerApi.OnlineGame.State.GameResults(
            role = TODO(),
            playersList = emptyKoneList(),
            userIndex = 0u,
            results = emptyKoneList(),
        ),
) : GameResultsComponent {
    override val onCopyOnlineGameKey: () -> Unit = {}
    override val onCopyOnlineGameLink: () -> Unit = {}
    override val onExitOnlineGame: () -> Unit = {}
    
    override val gameState: StateFlow<ServerApi.OnlineGame.State.GameResults> = MutableStateFlow(initialGameState)
}