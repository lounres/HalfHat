package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundEditing

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.flow.StateFlow


public interface RoundEditingComponent {
    public val gameState: StateFlow<ServerApi.OnlineGame.State.RoundEditing>
    
    // TODO: Make UI NOT to serve logic again: UI should not provide list of the words manually
    public val onGuessed: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit
    // TODO: Make UI NOT to serve logic again: UI should not provide list of the words manually
    public val onNotGuessed: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit
    // TODO: Make UI NOT to serve logic again: UI should not provide list of the words manually
    public val onMistake: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit
    
    public val onConfirm: () -> Unit
}