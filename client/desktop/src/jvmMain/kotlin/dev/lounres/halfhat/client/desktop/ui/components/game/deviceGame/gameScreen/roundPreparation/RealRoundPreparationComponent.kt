package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundPreparation

import kotlinx.coroutines.flow.StateFlow


class RealRoundPreparationComponent(
    override val onExitGame: () -> Unit,
    
    override val speaker: StateFlow<String>,
    override val listener: StateFlow<String>,
    override val millisecondsLeft: StateFlow<UInt>,
) : RoundPreparationComponent