package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.toKoneMutableList
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView
import kotlinx.coroutines.flow.StateFlow


public class RealRoundEditingComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.Round.Editing>,
    
    override val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>,
    
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