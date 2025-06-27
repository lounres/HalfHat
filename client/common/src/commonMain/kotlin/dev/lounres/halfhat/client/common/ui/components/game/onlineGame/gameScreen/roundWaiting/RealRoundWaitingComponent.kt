package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundWaiting

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


public class RealRoundWaitingComponent(
    override val onFinishGame: () -> Unit,
    
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundWaiting>,
    
    override val onSpeakerReady: () -> Unit,
    override val onListenerReady: () -> Unit,
) : RoundWaitingComponent