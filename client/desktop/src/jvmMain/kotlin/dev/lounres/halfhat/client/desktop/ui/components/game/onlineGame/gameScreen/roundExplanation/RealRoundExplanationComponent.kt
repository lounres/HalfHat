package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundExplanation

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import kotlinx.coroutines.flow.StateFlow


class RealRoundExplanationComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundExplanation>,
    
    onExplanationResult: (GameStateMachine.WordExplanation.State) -> Unit,
) : RoundExplanationComponent {
    override val onGuessed: () -> Unit = { onExplanationResult(GameStateMachine.WordExplanation.State.Explained) }
    override val onNotGuessed: () -> Unit = { onExplanationResult(GameStateMachine.WordExplanation.State.NotExplained) }
    override val onMistake: () -> Unit = { onExplanationResult(GameStateMachine.WordExplanation.State.Mistake) }
}