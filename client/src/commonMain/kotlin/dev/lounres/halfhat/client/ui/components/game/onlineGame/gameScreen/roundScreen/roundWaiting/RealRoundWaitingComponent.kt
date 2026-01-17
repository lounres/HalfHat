package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundWaiting

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


public class RealRoundWaitingComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.Round.Waiting>,
    
    override val onSpeakerReady: () -> Unit,
    override val onListenerReady: () -> Unit,
) : RoundWaitingComponent