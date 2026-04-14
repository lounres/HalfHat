package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roomScreen

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.api.onlineGame.DictionaryId
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.scope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


public class RealRoomScreenComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.GameInitialisation>,
    
    override val onExitOnlineGame: () -> Unit,
    override val onCopyOnlineGameKey: () -> Unit,
    override val onCopyOnlineGameLink: () -> Unit,

    override val availableDictionaries: StateFlow<KoneList<DictionaryId.WithDescription>?>,
    override val onLoadServerDictionaries: () -> Unit,
    
    override val onStartGame: () -> Unit,
    
    onApplySettings: (ClientApi.SettingsBuilderPatch) -> Unit,
) : RoomScreenComponent {
    override val onApplySettings: () -> Unit = {
        scope {
            onApplySettings(
                ClientApi.SettingsBuilderPatch(
                    preparationTimeSeconds = preparationTimeSeconds.value,
                    explanationTimeSeconds = explanationTimeSeconds.value,
                    finalGuessTimeSeconds = finalGuessTimeSeconds.value,
                    strictMode = strictMode.value,
                    cachedEndConditionWordsNumber = cachedEndConditionWordsNumber.value,
                    cachedEndConditionCyclesNumber = cachedEndConditionCyclesNumber.value,
                    gameEndConditionType = gameEndConditionType.value,
                    wordsSource = when (val wordsSource = wordsSource.value) {
                        null -> null
                        RoomScreenComponent.WordsSource.Players -> ClientApi.WordsSource.Players
                        RoomScreenComponent.WordsSource.HostDictionary -> ClientApi.WordsSource.HostDictionary(hostDictionary.value ?: return@scope)
                        is RoomScreenComponent.WordsSource.ServerDictionary -> ClientApi.WordsSource.ServerDictionary(wordsSource.description.id)
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
            wordsSource.value = null
            hostDictionary.value = null
        }
    }
    
    override val preparationTimeSeconds: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val explanationTimeSeconds: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val finalGuessTimeSeconds: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val strictMode: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    override val cachedEndConditionWordsNumber: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val cachedEndConditionCyclesNumber: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val gameEndConditionType: MutableStateFlow<GameStateMachine.GameEndCondition.Type?> = MutableStateFlow(null)
    override val wordsSource: MutableStateFlow<RoomScreenComponent.WordsSource?> = MutableStateFlow(null)
    override val hostDictionary: MutableStateFlow<KoneList<String>?> = MutableStateFlow(null)
    
    override val onDiscardSettings: () -> Unit = {
        preparationTimeSeconds.value = null
        explanationTimeSeconds.value = null
        finalGuessTimeSeconds.value = null
        strictMode.value = null
        cachedEndConditionWordsNumber.value = null
        cachedEndConditionCyclesNumber.value = null
        gameEndConditionType.value = null
        wordsSource.value = null
        hostDictionary.value = null
    }
}