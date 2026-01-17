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
    
    onApplySettings: (ClientApi.SettingsBuilder) -> Unit,
) : RoomScreenComponent {
    override val onApplySettings: () -> Unit = {
        val settingsBuilder = gameState.value.settingsBuilder
        
        onApplySettings(
            ClientApi.SettingsBuilder(
                preparationTimeSeconds = preparationTimeSeconds.value ?: settingsBuilder.preparationTimeSeconds,
                explanationTimeSeconds = explanationTimeSeconds.value ?: settingsBuilder.explanationTimeSeconds,
                finalGuessTimeSeconds = finalGuessTimeSeconds.value ?: settingsBuilder.finalGuessTimeSeconds,
                strictMode = strictMode.value ?: settingsBuilder.strictMode,
                cachedEndConditionWordsNumber = cachedEndConditionWordsNumber.value ?: settingsBuilder.cachedEndConditionWordsNumber,
                cachedEndConditionCyclesNumber = cachedEndConditionCyclesNumber.value ?: settingsBuilder.cachedEndConditionCyclesNumber,
                gameEndConditionType = gameEndConditionType.value ?: settingsBuilder.gameEndConditionType,
                wordsSource = when (val wordSource = settingsBuilder.wordsSource) {
                    ServerApi.WordsSource.Players -> ClientApi.WordsSource.Players
                    is ServerApi.WordsSource.ServerDictionary -> ClientApi.WordsSource.ServerDictionary(wordSource.id)
                },
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