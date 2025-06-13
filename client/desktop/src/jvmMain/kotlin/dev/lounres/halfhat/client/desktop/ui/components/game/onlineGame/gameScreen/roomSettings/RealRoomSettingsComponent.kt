package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomSettings

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import kotlinx.coroutines.flow.MutableStateFlow


class RealRoomSettingsComponent(
    onApplySettings: (ClientApi.SettingsBuilder) -> Unit,
    override val onDiscardSettings: () -> Unit,
    
    initialPreparationTimeSeconds: UInt,
    initialExplanationTimeSeconds: UInt,
    initialFinalGuessTimeSeconds: UInt,
    initialStrictMode: Boolean,
    initialCachedEndConditionWordsNumber: UInt,
    initialCachedEndConditionCyclesNumber: UInt,
    initialGameEndConditionType: GameStateMachine.GameEndCondition.Type,
) : RoomSettingsComponent {
    override val onApplySettings: () -> Unit = {
        onApplySettings(
            ClientApi.SettingsBuilder(
                preparationTimeSeconds = preparationTimeSeconds.value,
                explanationTimeSeconds = explanationTimeSeconds.value,
                finalGuessTimeSeconds = finalGuessTimeSeconds.value,
                strictMode = strictMode.value,
                cachedEndConditionWordsNumber = cachedEndConditionWordsNumber.value,
                cachedEndConditionCyclesNumber = cachedEndConditionCyclesNumber.value,
                gameEndConditionType = gameEndConditionType.value,
                wordsSource = ClientApi.WordsSource.Players,
            )
        )
    }
    
    override val preparationTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialPreparationTimeSeconds)
    override val explanationTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialExplanationTimeSeconds)
    override val finalGuessTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(initialFinalGuessTimeSeconds)
    override val strictMode: MutableStateFlow<Boolean> = MutableStateFlow(initialStrictMode)
    override val cachedEndConditionWordsNumber: MutableStateFlow<UInt> = MutableStateFlow(initialCachedEndConditionWordsNumber)
    override val cachedEndConditionCyclesNumber: MutableStateFlow<UInt> = MutableStateFlow(initialCachedEndConditionCyclesNumber)
    override val gameEndConditionType: MutableStateFlow<GameStateMachine.GameEndCondition.Type> = MutableStateFlow(initialGameEndConditionType)
}