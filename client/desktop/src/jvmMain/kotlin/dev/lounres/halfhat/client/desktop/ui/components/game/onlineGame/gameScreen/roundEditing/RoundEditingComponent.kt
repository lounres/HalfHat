package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundEditing

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.flow.StateFlow


interface RoundEditingComponent {
    val gameState: StateFlow<ServerApi.OnlineGame.State.RoundEditing>
    
    val onGuessed: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit
    val onNotGuessed: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit
    val onMistake: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit
    
    val onConfirm: () -> Unit
}