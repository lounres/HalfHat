package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundWaiting

import dev.lounres.halfhat.api.server.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.koneListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeRoundWaitingComponent(
    initialGameState: ServerApi.OnlineGame.State.RoundWaiting =
        ServerApi.OnlineGame.State.RoundWaiting(
            role = TODO(),
            playersList = koneListOf(),
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
            explanationScores = koneListOf(),
            guessingScores = koneListOf(),
            speakerReady = false,
            listenerReady = false,
        ),
) : RoundWaitingComponent {
    override val onFinishGame: () -> Unit = {}
    override val onCopyOnlineGameKey: () -> Unit = {}
    override val onCopyOnlineGameLink: () -> Unit = {}
    override val onExitOnlineGame: () -> Unit = {}
    
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundWaiting> = MutableStateFlow(initialGameState)
    
    override val onSpeakerReady: () -> Unit = {}
    override val onListenerReady: () -> Unit = {}
}