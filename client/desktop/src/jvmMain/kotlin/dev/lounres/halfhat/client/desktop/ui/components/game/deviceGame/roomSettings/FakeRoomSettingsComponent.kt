package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomSettings

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import kotlinx.coroutines.flow.MutableStateFlow


class FakeRoomSettingsComponent(
    initialSettingsBuilder: GameStateMachine.GameSettings.Builder<GameStateMachine.WordsProvider>,
) : RoomSettingsComponent {
    override val onApplySettings: () -> Unit = {}
    override val onDiscardSettings: () -> Unit = {}
    
    override val preparationTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.preparationTimeSeconds)
    override val explanationTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.explanationTimeSeconds)
    override val finalGuessTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.finalGuessTimeSeconds)
    override val strictMode: MutableStateFlow<Boolean> = MutableStateFlow(initialSettingsBuilder.strictMode)
    override val cachedEndConditionWordsNumber: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.cachedEndConditionWordsNumber)
    override val cachedEndConditionCyclesNumber: MutableStateFlow<UInt> = MutableStateFlow(initialSettingsBuilder.cachedEndConditionCyclesNumber)
    override val gameEndConditionType: MutableStateFlow<GameStateMachine.GameEndCondition.Type> = MutableStateFlow(initialSettingsBuilder.gameEndConditionType)
}