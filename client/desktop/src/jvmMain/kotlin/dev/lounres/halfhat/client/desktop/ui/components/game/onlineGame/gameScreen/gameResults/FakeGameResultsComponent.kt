package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.gameResults

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.empty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeGameResultsComponent(
    initialGameState: ServerApi.OnlineGame.State.GameResults =
        ServerApi.OnlineGame.State.GameResults(
            role = TODO(),
            playersList = KoneList.empty(),
            results = KoneList.empty(),
        ),
) : GameResultsComponent {
    override val gameState: StateFlow<ServerApi.OnlineGame.State.GameResults> = MutableStateFlow(initialGameState)
}