package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomSettings

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import kotlinx.coroutines.flow.MutableStateFlow


class RealRoomSettingsComponent(
    initialSettingsBuilder: GameStateMachine.GameSettingsBuilder<GameStateMachine.WordsProvider>,
    onUpdateSettingsBuilder: (GameStateMachine.GameSettingsBuilder<GameStateMachine.WordsProvider>) -> Unit,
    onExitSettings: () -> Unit,
) : RoomSettingsComponent {
    override val onApplySettings: () -> Unit = {
        onUpdateSettingsBuilder(
            GameStateMachine.GameSettingsBuilder<GameStateMachine.WordsProvider>(
                preparationTimeSeconds = preparationTimeSeconds.value,
                explanationTimeSeconds = explanationTimeSeconds.value,
                finalGuessTimeSeconds = finalGuessTimeSeconds.value,
                strictMode = strictMode.value,
                cachedEndConditionWordsNumber = cachedEndConditionWordsNumber.value,
                cachedEndConditionCyclesNumber = cachedEndConditionCyclesNumber.value,
                gameEndConditionType = gameEndConditionType.value,
                wordsSource = initialSettingsBuilder.wordsSource,
            )
        )
        onExitSettings()
    }
    override val onDiscardSettings: () -> Unit = onExitSettings
    
    override val preparationTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.preparationTimeSeconds)
    override val explanationTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.explanationTimeSeconds)
    override val finalGuessTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.finalGuessTimeSeconds)
    override val strictMode: MutableStateFlow<Boolean> = MutableStateFlow(initialSettingsBuilder.strictMode)
    override val cachedEndConditionWordsNumber: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.cachedEndConditionWordsNumber)
    override val cachedEndConditionCyclesNumber: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.cachedEndConditionCyclesNumber)
    override val gameEndConditionType: MutableStateFlow<GameStateMachine.GameEndCondition.Type> = MutableStateFlow(initialSettingsBuilder.gameEndConditionType)
}