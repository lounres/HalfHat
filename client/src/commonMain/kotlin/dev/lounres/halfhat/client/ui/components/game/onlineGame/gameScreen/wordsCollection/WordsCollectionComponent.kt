package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.wordsCollection

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow


public interface WordsCollectionComponent {
    public val onExitOnlineGame: () -> Unit
    public val onCopyOnlineGameKey: () -> Unit
    public val onCopyOnlineGameLink: () -> Unit
    
    public val currentWords: KoneMutableAsynchronousHubView<KoneList<KoneMutableAsynchronousHubView<String, *>>, *>
    public val onSubmit: () -> Unit
    
    public val gameState: StateFlow<ServerApi.OnlineGame.State.PlayersWordsCollection>
    
    public val coroutineScope: CoroutineScope
    public val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>
    public val openAdditionalCard: KoneMutableAsynchronousHubView<Boolean, *>
    public val additionalCard: KoneMutableAsynchronousHubView<AdditionalCard, *>
    
    enum class AdditionalCard {
        PlayersReadiness, Settings,
    }
}