package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundPreparation

import dev.lounres.halfhat.api.server.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.emptyKoneList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeRoundPreparationComponent(
    initialGameState: ServerApi.OnlineGame.State.RoundPreparation =
        ServerApi.OnlineGame.State.RoundPreparation(
            role = TODO(),
            playersList = emptyKoneList(),
            userIndex = 0u,
            settings = ServerApi.Settings(
                preparationTimeSeconds = 0u,
                explanationTimeSeconds = 0u,
                finalGuessTimeSeconds = 0u,
                strictMode = true,
                gameEndCondition = GameStateMachine.GameEndCondition.Words(100u),
            ),
            roundNumber = 0u,
            cycleNumber = 0u,
            speakerIndex = 0u,
            listenerIndex = 0u,
            explanationScores = emptyKoneList(),
            guessingScores = emptyKoneList(),
            millisecondsLeft = 29_000u,
        ),
) : RoundPreparationComponent {
    override val onCopyOnlineGameKey: () -> Unit = {}
    override val onCopyOnlineGameLink: () -> Unit = {}
    override val onExitOnlineGame: () -> Unit = {}
    
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundPreparation> = MutableStateFlow(initialGameState)
}