package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomSettings

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import kotlinx.coroutines.flow.MutableStateFlow


interface RoomSettingsComponent {
    val onApplySettings: () -> Unit
    val onDiscardSettings: () -> Unit
    val onCopyOnlineGameKey: () -> Unit
    val onCopyOnlineGameLink: () -> Unit
    val onExitOnlineGame: () -> Unit
    
    val preparationTimeSeconds: MutableStateFlow<UInt>
    val explanationTimeSeconds: MutableStateFlow<UInt>
    val finalGuessTimeSeconds: MutableStateFlow<UInt>
    val strictMode: MutableStateFlow<Boolean>
    val cachedEndConditionWordsNumber: MutableStateFlow<UInt>
    val cachedEndConditionCyclesNumber: MutableStateFlow<UInt>
    val gameEndConditionType: MutableStateFlow<GameStateMachine.GameEndCondition.Type>
//    val wordsSource // TODO: Implement different words sources
}