package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame

import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.previewScreen.PreviewScreenComponent
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.kone.state.KoneState
import kotlinx.coroutines.flow.StateFlow


enum class ConnectionStatus {
    Connected, Disconnected;
}

interface OnlineGamePageComponent {
    val onExitOnlineGameMode: () -> Unit
    
    val connectionStatus: StateFlow<ConnectionStatus>
    
    val childStack: KoneState<ChildrenStack<*, Child>>
    
    sealed interface Child {
        data class PreviewScreen(val component: PreviewScreenComponent) : Child
        data class GameScreen(val component: GameScreenComponent) : Child
    }
}