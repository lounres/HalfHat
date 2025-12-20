package dev.lounres.halfhat.client.common.ui.components.game.onlineGame

import dev.lounres.halfhat.client.common.logic.components.game.onlineGame.ConnectionStatus
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.previewScreen.PreviewScreenComponent
import dev.lounres.halfhat.client.components.navigation.ChildrenStack
import dev.lounres.kone.hub.KoneAsynchronousHub
import kotlinx.coroutines.flow.StateFlow


public interface OnlineGamePageComponent {
    public val onExitOnlineGameMode: () -> Unit
    
    public val connectionStatus: StateFlow<ConnectionStatus>
    
    public val childStack: KoneAsynchronousHub<ChildrenStack<*, Child>>
    
    public sealed interface Child {
        public data class PreviewScreen(val component: PreviewScreenComponent) : Child
        public data class GameScreen(val component: GameScreenComponent) : Child
    }
}