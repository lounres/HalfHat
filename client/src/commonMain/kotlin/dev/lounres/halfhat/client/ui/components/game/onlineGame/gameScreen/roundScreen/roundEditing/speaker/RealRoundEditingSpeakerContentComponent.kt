package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.speaker

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.toKoneMutableList
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import kotlinx.coroutines.flow.StateFlow


class RealRoundEditingSpeakerContentComponent(
    override val userRole: StateFlow<ServerApi.OnlineGame.Role.Round.Editing.RoundRole.Speaker>,
    
    override val darkTheme: KoneMutableAsynchronousHub<DarkTheme>,
    
    onUpdateExplanationResults: (KoneList<GameStateMachine.WordExplanation>) -> Unit,
    override val onConfirm: () -> Unit,
) : RoundEditingSpeakerContentComponent {
    override val onGuessed: (UInt) -> Unit = { wordIndex ->
        onUpdateExplanationResults(
            userRole.value.wordsToEdit.toKoneMutableList().apply {
                this[wordIndex] = GameStateMachine.WordExplanation(this[wordIndex].word, GameStateMachine.WordExplanation.State.Explained)
            }
        )
    }
    override val onNotGuessed: (UInt) -> Unit = { wordIndex ->
        onUpdateExplanationResults(
            userRole.value.wordsToEdit.toKoneMutableList().apply {
                this[wordIndex] = GameStateMachine.WordExplanation(this[wordIndex].word, GameStateMachine.WordExplanation.State.NotExplained)
            }
        )
    }
    override val onMistake: (UInt) -> Unit = { wordIndex ->
        onUpdateExplanationResults(
            userRole.value.wordsToEdit.toKoneMutableList().apply {
                this[wordIndex] = GameStateMachine.WordExplanation(this[wordIndex].word, GameStateMachine.WordExplanation.State.Mistake)
            }
        )
    }
}