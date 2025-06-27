package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundEditing

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.toKoneMutableList
import kotlinx.coroutines.flow.StateFlow


public class RealRoundEditingComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundEditing>,
    
    onUpdateExplanationResults: (KoneList<GameStateMachine.WordExplanation>) -> Unit,
    
    override val onConfirm: () -> Unit,
) : RoundEditingComponent {
    // TODO: Make UI NOT to serve logic again: UI should not provide list of the words manually
    override val onGuessed: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit = { wordsToEdit, wordIndex ->
        onUpdateExplanationResults(
            wordsToEdit.toKoneMutableList().apply {
                this[wordIndex] = GameStateMachine.WordExplanation(this[wordIndex].word, GameStateMachine.WordExplanation.State.Explained)
            }
        )
    }
    // TODO: Make UI NOT to serve logic again: UI should not provide list of the words manually
    override val onNotGuessed: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit = { wordsToEdit, wordIndex ->
        onUpdateExplanationResults(
            wordsToEdit.toKoneMutableList().apply {
                this[wordIndex] = GameStateMachine.WordExplanation(this[wordIndex].word, GameStateMachine.WordExplanation.State.NotExplained)
            }
        )
    }
    // TODO: Make UI NOT to serve logic again: UI should not provide list of the words manually
    override val onMistake: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit = { wordsToEdit, wordIndex ->
        onUpdateExplanationResults(
            wordsToEdit.toKoneMutableList().apply {
                this[wordIndex] = GameStateMachine.WordExplanation(this[wordIndex].word, GameStateMachine.WordExplanation.State.Mistake)
            }
        )
    }
}