package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roomScreen

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


public class RealRoomScreenComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.GameInitialisation>,
    
    override val onExitOnlineGame: () -> Unit,
    override val onCopyOnlineGameKey: () -> Unit,
    override val onCopyOnlineGameLink: () -> Unit,
    
    override val onStartGame: () -> Unit,
    
    onApplySettings: (ClientApi.SettingsBuilder.Patch) -> Unit,
) : RoomScreenComponent {
    override val onApplySettings: () -> Unit = {
        onApplySettings(
            ClientApi.SettingsBuilder.Patch(
                preparationTimeSeconds = preparationTimeSeconds.value,
                explanationTimeSeconds = explanationTimeSeconds.value,
                finalGuessTimeSeconds = finalGuessTimeSeconds.value,
                strictMode = strictMode.value,
                cachedEndConditionWordsNumber = cachedEndConditionWordsNumber.value,
                cachedEndConditionCyclesNumber = cachedEndConditionCyclesNumber.value,
                gameEndConditionType = gameEndConditionType.value,
                wordsSource = null,
            )
        )
        
        preparationTimeSeconds.value = null
        explanationTimeSeconds.value = null
        finalGuessTimeSeconds.value = null
        strictMode.value = null
        cachedEndConditionWordsNumber.value = null
        cachedEndConditionCyclesNumber.value = null
        gameEndConditionType.value = null
    }
    
    override val preparationTimeSeconds: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val explanationTimeSeconds: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val finalGuessTimeSeconds: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val strictMode: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    override val cachedEndConditionWordsNumber: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val cachedEndConditionCyclesNumber: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val gameEndConditionType: MutableStateFlow<GameStateMachine.GameEndCondition.Type?> = MutableStateFlow(null)
    
    override val onDiscardSettings: () -> Unit = {
        preparationTimeSeconds.value = null
        explanationTimeSeconds.value = null
        finalGuessTimeSeconds.value = null
        strictMode.value = null
        cachedEndConditionWordsNumber.value = null
        cachedEndConditionCyclesNumber.value = null
        gameEndConditionType.value = null
    }
}