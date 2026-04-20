package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.gameInitialisation

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.api.onlineGame.DictionaryId
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.toKoneMutableList
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.scope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


public class RealGameInitialisationComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.GameInitialisation>,
    
    override val onExitOnlineGame: () -> Unit,
    override val onCopyOnlineGameKey: () -> Unit,
    override val onCopyOnlineGameLink: () -> Unit,

    override val availableDictionaries: StateFlow<KoneList<DictionaryId.WithDescription>?>,
    override val onLoadServerDictionaries: () -> Unit,
    
    override val onStartGame: () -> Unit,
    
    onApplySettings: (KoneList<ClientApi.GlobalRole>, ClientApi.SettingsBuilderPatch, ClientApi.ExtraSettingsPatch) -> Unit,
) : GameInitialisationComponent {
    override val onUpdateRoles: (UInt, ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole) -> Unit = { index, globalRole ->
        onApplySettings(
            gameState.value.playersList.map {
                when (it.globalRole) {
                    ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Player -> ClientApi.GlobalRole.Player
                    ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Spectator -> ClientApi.GlobalRole.Spectator
                }
            }.toKoneMutableList().apply {
                this[index] = when (globalRole) {
                    ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Player -> ClientApi.GlobalRole.Player
                    ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Spectator -> ClientApi.GlobalRole.Spectator
                }
            },
            ClientApi.SettingsBuilderPatch(
                preparationTimeSeconds = null,
                explanationTimeSeconds = null,
                finalGuessTimeSeconds = null,
                strictMode = null,
                cachedEndConditionWordsNumber = null,
                cachedEndConditionCyclesNumber = null,
                gameEndConditionType = null,
                wordsSource = null,
            ),
            ClientApi.ExtraSettingsPatch(
                showWordsStatistic = null,
                showLeaderboardPermutation = null,
            ),
        )
    }

    override val onApplySettings: () -> Unit = onApplySettings@ {
        onApplySettings(
            gameState.value.playersList.map {
                when (it.globalRole) {
                    ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Player -> ClientApi.GlobalRole.Player
                    ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Spectator -> ClientApi.GlobalRole.Spectator
                }
            },
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
                    GameInitialisationComponent.WordsSource.Players -> ClientApi.WordsSource.Players
                    GameInitialisationComponent.WordsSource.HostDictionary -> ClientApi.WordsSource.HostDictionary(hostDictionary.value ?: return@onApplySettings)
                    is GameInitialisationComponent.WordsSource.ServerDictionary -> ClientApi.WordsSource.ServerDictionary(wordsSource.description.id)
                },
            ),
            ClientApi.ExtraSettingsPatch(
                showWordsStatistic = showWordsStatistic.value,
                showLeaderboardPermutation = showLeaderboardPermutation.value,
            ),
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
        showWordsStatistic.value = null
        showLeaderboardPermutation.value = null
    }
    
    override val preparationTimeSeconds: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val explanationTimeSeconds: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val finalGuessTimeSeconds: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val strictMode: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    override val cachedEndConditionWordsNumber: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val cachedEndConditionCyclesNumber: MutableStateFlow<UInt?> = MutableStateFlow(null)
    override val gameEndConditionType: MutableStateFlow<GameStateMachine.GameEndCondition.Type?> = MutableStateFlow(null)
    override val wordsSource: MutableStateFlow<GameInitialisationComponent.WordsSource?> = MutableStateFlow(null)
    override val hostDictionary: MutableStateFlow<KoneList<String>?> = MutableStateFlow(null)
    override val showWordsStatistic: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    override val showLeaderboardPermutation: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    
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
        showWordsStatistic.value = null
        showLeaderboardPermutation.value = null
    }
}