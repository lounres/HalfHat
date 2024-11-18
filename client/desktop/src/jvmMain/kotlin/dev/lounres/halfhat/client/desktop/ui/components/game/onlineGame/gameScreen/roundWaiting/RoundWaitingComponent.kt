package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundWaiting

import dev.lounres.halfhat.api.server.ServerApi
import kotlinx.coroutines.flow.StateFlow


interface RoundWaitingComponent {
    val onFinishGame: () -> Unit
    val onCopyOnlineGameKey: () -> Unit
    val onCopyOnlineGameLink: () -> Unit
    val onExitOnlineGame: () -> Unit
    
    val gameState: StateFlow<ServerApi.OnlineGame.State.RoundWaiting>
    
    val onSpeakerReady: () -> Unit
    val onListenerReady: () -> Unit
}