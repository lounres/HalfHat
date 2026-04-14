package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.wordsCollection

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.collections.utils.filter
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow


class RealWordsCollectionComponent(
    componentContext: UIComponentContext,
    
    onSubmitWords: (KoneList<String>) -> Unit,
    
    override val onExitOnlineGame: () -> Unit,
    override val onCopyOnlineGameKey: () -> Unit,
    override val onCopyOnlineGameLink: () -> Unit,
    
    override val gameState: StateFlow<ServerApi.OnlineGame.State.PlayersWordsCollection>,
) : WordsCollectionComponent {
    override val currentWords: KoneMutableAsynchronousHub<KoneList<KoneMutableAsynchronousHub<String>>> =
        KoneMutableAsynchronousHub(KoneList.of(KoneMutableAsynchronousHub("")))
    override val onSubmit: () -> Unit = { onSubmitWords(currentWords.value.map { it.value }.filter { it.isNotEmpty() }) }
    
    override val coroutineScope: CoroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    override val darkTheme: KoneMutableAsynchronousHub<DarkTheme> = componentContext.settings.darkTheme
    override val openAdditionalCard: KoneMutableAsynchronousHub<Boolean> = KoneMutableAsynchronousHub(false)
    override val additionalCard: KoneMutableAsynchronousHub<WordsCollectionComponent.AdditionalCard> = KoneMutableAsynchronousHub(WordsCollectionComponent.AdditionalCard.PlayersReadiness)
}