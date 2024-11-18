package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundPreparation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeRoundPreparationComponent(
    initialSpeaker: String = "Tiger",
    initialListener: String = "Panther",
    initialMillisecondsLeft: UInt = 2_000u,
) : RoundPreparationComponent {
    override val onExitGame: () -> Unit = {}
    
    override val speaker: StateFlow<String> = MutableStateFlow(initialSpeaker)
    override val listener: StateFlow<String> = MutableStateFlow(initialListener)
    override val millisecondsLeft: StateFlow<UInt> = MutableStateFlow(initialMillisecondsLeft)
}