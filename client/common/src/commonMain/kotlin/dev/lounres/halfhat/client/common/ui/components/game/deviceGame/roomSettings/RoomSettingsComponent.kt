package dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomSettings

import dev.lounres.halfhat.client.common.logic.wordsProviders.DeviceGameWordsProviderID
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.flow.MutableStateFlow


public interface RoomSettingsComponent {
    public val onApplySettings: () -> Unit
    public val onDiscardSettings: () -> Unit
    
    public val preparationTimeSeconds: MutableStateFlow<String>
    public val explanationTimeSeconds: MutableStateFlow<String>
    public val finalGuessTimeSeconds: MutableStateFlow<String>
    public val strictMode: MutableStateFlow<Boolean>
    public val possibleWordsSources: KoneList<DeviceGameWordsProviderID>
    public val wordsSource: MutableStateFlow<GameStateMachine.WordsSource<DeviceGameWordsProviderID>>
    public val cachedEndConditionWordsNumber: MutableStateFlow<UInt>
    public val cachedEndConditionCyclesNumber: MutableStateFlow<UInt>
    public val gameEndConditionType: MutableStateFlow<GameStateMachine.GameEndCondition.Type>
}