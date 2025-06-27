package dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundEditing

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.flow.StateFlow


public interface RoundEditingComponent {
    public val onExitGame: () -> Unit
    
    public val wordsToEdit: StateFlow<KoneList<GameStateMachine.WordExplanation>>
    
    public val onGuessed: (UInt) -> Unit
    public val onNotGuessed: (UInt) -> Unit
    public val onMistake: (UInt) -> Unit
    
    public val onConfirm: () -> Unit
}