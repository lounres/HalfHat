package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundEditing

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.KoneList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeRoundEditingComponent(
    initialWordsToEdit: KoneList<GameStateMachine.WordExplanation> =
        KoneList(10u) { GameStateMachine.WordExplanation("word #$it", GameStateMachine.WordExplanation.State.entries.random()) },
) : RoundEditingComponent {
    override val onExitGame: () -> Unit = {}
    
    override val wordsToEdit: StateFlow<KoneList<GameStateMachine.WordExplanation>> = MutableStateFlow(initialWordsToEdit)
    
    override val onGuessed: (UInt) -> Unit = {}
    override val onNotGuessed: (UInt) -> Unit = {}
    override val onMistake: (UInt) -> Unit = {}
    
    override val onConfirm: () -> Unit = {}
}