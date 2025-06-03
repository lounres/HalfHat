package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomScreen

import dev.lounres.halfhat.api.server.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeRoomScreenComponent(
    initialGameState: ServerApi.OnlineGame.State.GameInitialisation =
        ServerApi.OnlineGame.State.GameInitialisation(
            role = TODO(),
            playersList = KoneList.of(
                ServerApi.PlayerDescription(name = "Panther", isOnline = true),
                ServerApi.PlayerDescription(name = "Tiger", isOnline = false),
            ),
            userIndex = 0u,
            settingsBuilder = ServerApi.SettingsBuilder(
                preparationTimeSeconds = 3u,
                explanationTimeSeconds = 40u,
                finalGuessTimeSeconds = 3u,
                strictMode = false,
                cachedEndConditionWordsNumber = 100u,
                cachedEndConditionCyclesNumber = 3u,
                gameEndConditionType = GameStateMachine.GameEndCondition.Type.Words,
                wordsSource = ServerApi.WordsSource.ServerDictionary("All words"),
            )
        ),
) : RoomScreenComponent {
    override val onCopyOnlineGameKey: () -> Unit = {}
    override val onCopyOnlineGameLink: () -> Unit = {}
    override val onExitOnlineGame: () -> Unit = {}
    
    override val gameStateFlow: StateFlow<ServerApi.OnlineGame.State.GameInitialisation> = MutableStateFlow(initialGameState)
    
    override val onOpenGameSettings: () -> Unit = {}
    override val onStartGame: () -> Unit = {}
}