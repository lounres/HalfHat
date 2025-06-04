package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundEditing

import dev.lounres.halfhat.api.server.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.toKoneMutableList
import kotlinx.coroutines.flow.StateFlow


class RealRoundEditingComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundEditing>,
    
    onUpdateExplanationResults: (KoneList<GameStateMachine.WordExplanation>) -> Unit,
    
    override val onConfirm: () -> Unit,
) : RoundEditingComponent {
    override val onGuessed: (UInt) -> Unit = {
        onUpdateExplanationResults(
            gameState.value.wordsToEdit.toKoneMutableList().apply {
                this[it] = GameStateMachine.WordExplanation(this[it].word, GameStateMachine.WordExplanation.State.Explained)
            }
        )
    }
    override val onNotGuessed: (UInt) -> Unit = {
        onUpdateExplanationResults(
            gameState.value.wordsToEdit.toKoneMutableList().apply {
                this[it] = GameStateMachine.WordExplanation(this[it].word, GameStateMachine.WordExplanation.State.NotExplained)
            }
        )
    }
    override val onMistake: (UInt) -> Unit = {
        onUpdateExplanationResults(
            gameState.value.wordsToEdit.toKoneMutableList().apply {
                this[it] = GameStateMachine.WordExplanation(this[it].word, GameStateMachine.WordExplanation.State.Mistake)
            }
        )
    }
}