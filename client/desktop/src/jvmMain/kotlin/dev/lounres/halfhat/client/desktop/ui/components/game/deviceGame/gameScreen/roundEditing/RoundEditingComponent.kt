package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundEditing

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.flow.StateFlow


interface RoundEditingComponent {
    val onExitGame: () -> Unit
    
    val wordsToEdit: StateFlow<KoneList<GameStateMachine.WordExplanation>>
    
    val onGuessed: (UInt) -> Unit
    val onNotGuessed: (UInt) -> Unit
    val onMistake: (UInt) -> Unit
    
    val onConfirm: () -> Unit
}