package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roomSettings

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import kotlinx.coroutines.flow.MutableStateFlow


public interface RoomSettingsComponent {
    public val onApplySettings: () -> Unit
    public val onDiscardSettings: () -> Unit
    
    public val preparationTimeSeconds: MutableStateFlow<UInt>
    public val explanationTimeSeconds: MutableStateFlow<UInt>
    public val finalGuessTimeSeconds: MutableStateFlow<UInt>
    public val strictMode: MutableStateFlow<Boolean>
    public val cachedEndConditionWordsNumber: MutableStateFlow<UInt>
    public val cachedEndConditionCyclesNumber: MutableStateFlow<UInt>
    public val gameEndConditionType: MutableStateFlow<GameStateMachine.GameEndCondition.Type>
//    val wordsSource // TODO: Implement different words sources
}