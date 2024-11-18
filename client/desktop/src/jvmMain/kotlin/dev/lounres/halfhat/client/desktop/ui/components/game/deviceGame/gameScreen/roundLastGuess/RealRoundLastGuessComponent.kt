package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundLastGuess

import kotlinx.coroutines.flow.StateFlow


class RealRoundLastGuessComponent(
    override val onExitGame: () -> Unit,
    
    override val speaker: StateFlow<String>,
    override val listener: StateFlow<String>,
    override val millisecondsLeft: StateFlow<UInt>,
    override val word: StateFlow<String>,
    
    override val onGuessed: () -> Unit,
    override val onNotGuessed: () -> Unit,
    override val onMistake: () -> Unit,
) : RoundLastGuessComponent