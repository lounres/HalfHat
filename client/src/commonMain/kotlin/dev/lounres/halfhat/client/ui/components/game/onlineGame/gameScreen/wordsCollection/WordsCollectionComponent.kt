package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.wordsCollection

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow


public interface WordsCollectionComponent {
    public val onExitOnlineGame: () -> Unit
    public val onCopyOnlineGameKey: () -> Unit
    public val onCopyOnlineGameLink: () -> Unit
    
    public val currentWords: KoneMutableAsynchronousHub<KoneList<KoneMutableAsynchronousHub<String>>>
    public val onSubmit: () -> Unit
    
    public val gameState: StateFlow<ServerApi.OnlineGame.State.PlayersWordsCollection>
    
    public val coroutineScope: CoroutineScope
    public val darkTheme: KoneMutableAsynchronousHub<DarkTheme>
    public val openAdditionalCard: KoneMutableAsynchronousHub<Boolean>
    public val additionalCard: KoneMutableAsynchronousHub<AdditionalCard>
    
    enum class AdditionalCard {
        PlayersReadiness, Settings,
    }
}