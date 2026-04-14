package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundExplanation

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.hub.KoneAsynchronousHub
import kotlinx.coroutines.flow.StateFlow


public class RealRoundExplanationComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.Round.Explanation>,
    
    override val darkTheme: KoneAsynchronousHub<DarkTheme>,
    onExplanationResult: (GameStateMachine.WordExplanation.State) -> Unit,
) : RoundExplanationComponent {
    override val onGuessed: () -> Unit = { onExplanationResult(GameStateMachine.WordExplanation.State.Explained) }
    override val onNotGuessed: () -> Unit = { onExplanationResult(GameStateMachine.WordExplanation.State.NotExplained) }
    override val onMistake: () -> Unit = { onExplanationResult(GameStateMachine.WordExplanation.State.Mistake) }
}