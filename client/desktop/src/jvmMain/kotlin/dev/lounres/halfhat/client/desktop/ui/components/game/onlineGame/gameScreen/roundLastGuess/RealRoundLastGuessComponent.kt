package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundLastGuess

import dev.lounres.halfhat.api.server.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import kotlinx.coroutines.flow.StateFlow


class RealRoundLastGuessComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundLastGuess>,
    
    onExplanationResult: (GameStateMachine.WordExplanation.State) -> Unit,
) : RoundLastGuessComponent {
    override val onGuessed: () -> Unit = { onExplanationResult(GameStateMachine.WordExplanation.State.Explained) }
    override val onNotGuessed: () -> Unit = { onExplanationResult(GameStateMachine.WordExplanation.State.NotExplained) }
    override val onMistake: () -> Unit = { onExplanationResult(GameStateMachine.WordExplanation.State.Mistake) }
}