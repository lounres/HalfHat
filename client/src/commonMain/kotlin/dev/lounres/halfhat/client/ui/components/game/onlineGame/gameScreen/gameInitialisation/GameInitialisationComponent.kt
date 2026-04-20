package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.gameInitialisation

import dev.lounres.halfhat.api.onlineGame.DictionaryId
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


public interface GameInitialisationComponent {
    public val onExitOnlineGame: () -> Unit
    public val onCopyOnlineGameKey: () -> Unit
    public val onCopyOnlineGameLink: () -> Unit
    
    public val gameState: StateFlow<ServerApi.OnlineGame.State.GameInitialisation>
    
    public val onStartGame: () -> Unit

    public val onUpdateRoles: (UInt, ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole) -> Unit
    
    public val preparationTimeSeconds: MutableStateFlow<UInt?>
    public val explanationTimeSeconds: MutableStateFlow<UInt?>
    public val finalGuessTimeSeconds: MutableStateFlow<UInt?>
    public val strictMode: MutableStateFlow<Boolean?>
    public val cachedEndConditionWordsNumber: MutableStateFlow<UInt?>
    public val cachedEndConditionCyclesNumber: MutableStateFlow<UInt?>
    public val gameEndConditionType: MutableStateFlow<GameStateMachine.GameEndCondition.Type?>
    public val wordsSource: MutableStateFlow<WordsSource?>
    public val hostDictionary: MutableStateFlow<KoneList<String>?>
    public val showWordsStatistic: MutableStateFlow<Boolean?> // TODO: Use in settings!!!
    public val showLeaderboardPermutation: MutableStateFlow<Boolean?> // TODO: Use in settings!!!

    public val availableDictionaries: StateFlow<KoneList<DictionaryId.WithDescription>?>
    public val onLoadServerDictionaries: () -> Unit
    
    public val onApplySettings: () -> Unit
    public val onDiscardSettings: () -> Unit
    
    public sealed interface WordsSource {
        public data object Players : WordsSource
        public data object HostDictionary : WordsSource
        public data class ServerDictionary(
            val description: DictionaryId.WithDescription,
        ) : WordsSource
    }
}