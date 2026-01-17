package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundWaiting

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


public interface RoundWaitingComponent {
    public val gameState: StateFlow<ServerApi.OnlineGame.State.Round.Waiting>
    
    public val onSpeakerReady: () -> Unit
    public val onListenerReady: () -> Unit
}