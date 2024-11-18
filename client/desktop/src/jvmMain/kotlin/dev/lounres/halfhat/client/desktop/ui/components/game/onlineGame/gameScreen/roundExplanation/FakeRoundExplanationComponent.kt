package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundExplanation

import dev.lounres.halfhat.api.server.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.emptyKoneList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeRoundExplanationComponent(
    initialGameState: ServerApi.OnlineGame.State.RoundExplanation =
        ServerApi.OnlineGame.State.RoundExplanation(
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
) : RoundExplanationComponent {
    override val onCopyOnlineGameKey: () -> Unit = {}
    override val onCopyOnlineGameLink: () -> Unit = {}
    override val onExitOnlineGame: () -> Unit = {}
    
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundExplanation> = MutableStateFlow(initialGameState)
    
    override val onGuessed: () -> Unit = {}
    override val onNotGuessed: () -> Unit = {}
    override val onMistake: () -> Unit = {}
}