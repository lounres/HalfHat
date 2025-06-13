package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundEditing

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.toKoneMutableList
import kotlinx.coroutines.flow.StateFlow


class RealRoundEditingComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundEditing>,
    
    onUpdateExplanationResults: (KoneList<GameStateMachine.WordExplanation>) -> Unit,
    
    override val onConfirm: () -> Unit,
) : RoundEditingComponent {
    override val onGuessed: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit = { wordsToEdit, wordIndex ->
        onUpdateExplanationResults(
            wordsToEdit.toKoneMutableList().apply {
                this[wordIndex] = GameStateMachine.WordExplanation(this[wordIndex].word, GameStateMachine.WordExplanation.State.Explained)
            }
        )
    }
    override val onNotGuessed: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit = { wordsToEdit, wordIndex ->
        onUpdateExplanationResults(
            wordsToEdit.toKoneMutableList().apply {
                this[wordIndex] = GameStateMachine.WordExplanation(this[wordIndex].word, GameStateMachine.WordExplanation.State.NotExplained)
            }
        )
    }
    override val onMistake: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit = { wordsToEdit, wordIndex ->
        onUpdateExplanationResults(
            wordsToEdit.toKoneMutableList().apply {
                this[wordIndex] = GameStateMachine.WordExplanation(this[wordIndex].word, GameStateMachine.WordExplanation.State.Mistake)
            }
        )
    }
}