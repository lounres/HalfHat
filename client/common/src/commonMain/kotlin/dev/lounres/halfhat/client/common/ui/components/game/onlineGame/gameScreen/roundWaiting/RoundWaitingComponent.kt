package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundWaiting

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


public interface RoundWaitingComponent {
    public val onFinishGame: () -> Unit
    
    public val gameState: StateFlow<ServerApi.OnlineGame.State.RoundWaiting>
    
    public val onSpeakerReady: () -> Unit
    public val onListenerReady: () -> Unit
}