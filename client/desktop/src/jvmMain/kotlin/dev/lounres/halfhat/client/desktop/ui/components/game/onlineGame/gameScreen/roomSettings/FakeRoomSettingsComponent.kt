package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomSettings

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import kotlinx.coroutines.flow.MutableStateFlow


class FakeRoomSettingsComponent(
    initialPreparationTimeSeconds: UInt,
    initialExplanationTimeSeconds: UInt,
    initialFinalGuessTimeSeconds: UInt,
    initialStrictMode: Boolean,
    initialCachedEndConditionWordsNumber: UInt,
    initialCachedEndConditionCyclesNumber: UInt,
    initialGameEndCondition: GameStateMachine.GameEndCondition.Type,
) : RoomSettingsComponent {
    override val onApplySettings: () -> Unit = {}
    override val onDiscardSettings: () -> Unit = {}
    
    override val preparationTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialPreparationTimeSeconds)
    override val explanationTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialExplanationTimeSeconds)
    override val finalGuessTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialFinalGuessTimeSeconds)
    override val strictMode: MutableStateFlow<Boolean> = MutableStateFlow(initialStrictMode)
    override val cachedEndConditionWordsNumber: MutableStateFlow<UInt> = MutableStateFlow(initialCachedEndConditionWordsNumber)
    override val cachedEndConditionCyclesNumber: MutableStateFlow<UInt> = MutableStateFlow(initialCachedEndConditionCyclesNumber)
    override val gameEndConditionType: MutableStateFlow<GameStateMachine.GameEndCondition.Type> = MutableStateFlow(initialGameEndCondition)
}