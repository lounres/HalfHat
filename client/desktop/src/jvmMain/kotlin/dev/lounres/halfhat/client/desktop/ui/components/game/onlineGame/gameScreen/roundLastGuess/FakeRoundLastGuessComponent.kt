package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundLastGuess

import dev.lounres.halfhat.api.server.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.empty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeRoundLastGuessComponent(
    initialGameState: ServerApi.OnlineGame.State.RoundLastGuess =
        ServerApi.OnlineGame.State.RoundLastGuess(
            role = TODO(),
            playersList = KoneList.empty(),
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
            explanationScores = KoneList.empty(),
            guessingScores = KoneList.empty(),
            millisecondsLeft = 29_000u,
        ),
) : RoundLastGuessComponent {
    override val onCopyOnlineGameKey: () -> Unit = {}
    override val onCopyOnlineGameLink: () -> Unit = {}
    override val onExitOnlineGame: () -> Unit = {}
    
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundLastGuess> = MutableStateFlow(initialGameState)
    
    override val onGuessed: () -> Unit = {}
    override val onNotGuessed: () -> Unit = {}
    override val onMistake: () -> Unit = {}
}