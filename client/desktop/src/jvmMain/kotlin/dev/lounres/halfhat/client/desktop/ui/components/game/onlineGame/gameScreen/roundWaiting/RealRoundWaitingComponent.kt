package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundWaiting

import dev.lounres.halfhat.api.server.ServerApi
import kotlinx.coroutines.flow.StateFlow


class RealRoundWaitingComponent(
    override val onFinishGame: () -> Unit,
    override val onCopyOnlineGameKey: () -> Unit,
    override val onCopyOnlineGameLink: () -> Unit,
    override val onExitOnlineGame: () -> Unit,
    
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundWaiting>,
    
    override val onSpeakerReady: () -> Unit,
    override val onListenerReady: () -> Unit,
) : RoundWaitingComponent