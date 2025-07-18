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
    override val onApplySettings: () -> Unit = onApplySettings@{
        showErrorForPreparationTimeSeconds.value = true
        showErrorForExplanationTimeSeconds.value = true
        showErrorForFinalGuessTimeSeconds.value = true
        showErrorForCachedEndConditionWordsNumber.value = true
        showErrorForCachedEndConditionCyclesNumber.value = true
        onUpdateSettingsBuilder(
            GameStateMachine.GameSettings.Builder(
                preparationTimeSeconds = preparationTimeSeconds.value.let { if (it.isBlank()) return@onApplySettings else it.toUInt() },
                explanationTimeSeconds = explanationTimeSeconds.value.let { if (it.isBlank()) return@onApplySettings else it.toUInt() },
                finalGuessTimeSeconds = finalGuessTimeSeconds.value.let { if (it.isBlank()) return@onApplySettings else it.toUInt() },
                strictMode = strictMode.value,
                cachedEndConditionWordsNumber = cachedEndConditionWordsNumber.value.let { if (it.isBlank() || it == "0") return@onApplySettings else it.toUInt() },
                cachedEndConditionCyclesNumber = cachedEndConditionCyclesNumber.value.let { if (it.isBlank() || it == "0") return@onApplySettings else it.toUInt() },
                gameEndConditionType = gameEndConditionType.value,
                wordsSource = wordsSource.value,
            )
        )
        onExitSettings()
    }
    override val onDiscardSettings: () -> Unit = onExitSettings
    
    override val preparationTimeSeconds: MutableStateFlow<String> = MutableStateFlow(initialSettingsBuilder.preparationTimeSeconds.toString())
    override val showErrorForPreparationTimeSeconds: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val explanationTimeSeconds: MutableStateFlow<String> = MutableStateFlow(initialSettingsBuilder.explanationTimeSeconds.toString())
    override val showErrorForExplanationTimeSeconds: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val finalGuessTimeSeconds: MutableStateFlow<String> = MutableStateFlow(initialSettingsBuilder.finalGuessTimeSeconds.toString())
    override val showErrorForFinalGuessTimeSeconds: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val strictMode: MutableStateFlow<Boolean> = MutableStateFlow(initialSettingsBuilder.strictMode)
    override val wordsSource: MutableStateFlow<GameStateMachine.WordsSource<DeviceGameWordsProviderID>> = MutableStateFlow(initialSettingsBuilder.wordsSource) // TODO: Add check that if the words source is custom, then it lies in the [possibleWordsSources]
    override val cachedEndConditionWordsNumber: MutableStateFlow<String> = MutableStateFlow(initialSettingsBuilder.cachedEndConditionWordsNumber.toString())
    override val showErrorForCachedEndConditionWordsNumber: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val cachedEndConditionCyclesNumber: MutableStateFlow<String> = MutableStateFlow(initialSettingsBuilder.cachedEndConditionCyclesNumber.toString())
    override val showErrorForCachedEndConditionCyclesNumber: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val gameEndConditionType: MutableStateFlow<GameStateMachine.GameEndCondition.Type> = MutableStateFlow(initialSettingsBuilder.gameEndConditionType)
}