package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundWaiting

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.StateFlow


class RealRoundWaitingComponent(
    override val onFinishGame: () -> Unit,
    
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundWaiting>,
    
    override val onSpeakerReady: () -> Unit,
    override val onListenerReady: () -> Unit,
) : RoundWaitingComponent