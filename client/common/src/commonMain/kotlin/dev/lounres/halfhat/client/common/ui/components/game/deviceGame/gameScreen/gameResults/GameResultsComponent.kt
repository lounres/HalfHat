package dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.gameResults

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.flow.StateFlow


public interface GameResultsComponent {
    public val onExitGame: () -> Unit
    
    public val results: StateFlow<KoneList<GameStateMachine.PersonalResult<String>>>
}