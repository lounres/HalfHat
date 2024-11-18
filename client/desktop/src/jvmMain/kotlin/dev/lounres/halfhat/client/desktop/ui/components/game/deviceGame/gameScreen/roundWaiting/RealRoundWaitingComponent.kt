package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundWaiting

import kotlinx.coroutines.flow.StateFlow


class RealRoundWaitingComponent(
    override val onExitGame: () -> Unit,
    override val onFinishGame: () -> Unit,
    
    override val speaker: StateFlow<String>,
    override val listener: StateFlow<String>,
    override val onStartRound: () -> Unit,
) : RoundWaitingComponent