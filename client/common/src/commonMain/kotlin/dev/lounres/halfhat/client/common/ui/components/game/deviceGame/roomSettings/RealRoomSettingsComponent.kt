package dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomSettings

import dev.lounres.halfhat.client.common.logic.wordsProviders.DeviceGameWordsProviderID
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.flow.MutableStateFlow


public class RealRoomSettingsComponent(
    initialSettingsBuilder: GameStateMachine.GameSettings.Builder<DeviceGameWordsProviderID>,
    override val possibleWordsSources: KoneList<DeviceGameWordsProviderID>,
    onUpdateSettingsBuilder: (GameStateMachine.GameSettings.Builder<DeviceGameWordsProviderID>) -> Unit,
    onExitSettings: () -> Unit,
) : RoomSettingsComponent {
    override val onApplySettings: () -> Unit = {
        onUpdateSettingsBuilder(
            GameStateMachine.GameSettings.Builder(
                preparationTimeSeconds = preparationTimeSeconds.value,
                explanationTimeSeconds = explanationTimeSeconds.value,
                finalGuessTimeSeconds = finalGuessTimeSeconds.value,
                strictMode = strictMode.value,
                cachedEndConditionWordsNumber = cachedEndConditionWordsNumber.value,
                cachedEndConditionCyclesNumber = cachedEndConditionCyclesNumber.value,
                gameEndConditionType = gameEndConditionType.value,
                wordsSource = wordsSource.value,
            )
        )
        onExitSettings()
    }
    override val onDiscardSettings: () -> Unit = onExitSettings
    
    override val preparationTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.preparationTimeSeconds)
    override val explanationTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.explanationTimeSeconds)
    override val finalGuessTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.finalGuessTimeSeconds)
    override val strictMode: MutableStateFlow<Boolean> = MutableStateFlow(initialSettingsBuilder.strictMode)
    override val wordsSource: MutableStateFlow<GameStateMachine.WordsSource<DeviceGameWordsProviderID>> = MutableStateFlow(initialSettingsBuilder.wordsSource) // TODO: Add check that if the words source is custom, then it lies in the [possibleWordsSources]
    override val cachedEndConditionWordsNumber: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.cachedEndConditionWordsNumber)
    override val cachedEndConditionCyclesNumber: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.cachedEndConditionCyclesNumber)
    override val gameEndConditionType: MutableStateFlow<GameStateMachine.GameEndCondition.Type> = MutableStateFlow(initialSettingsBuilder.gameEndConditionType)
}