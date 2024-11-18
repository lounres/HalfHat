package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundWaiting

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeRoundWaitingComponent(
    initialSpeaker: String = "Tiger",
    initialListener: String = "Panther",
) : RoundWaitingComponent {
    override val onExitGame: () -> Unit = {}
    override val onFinishGame: () -> Unit = {}
    
    override val speaker: StateFlow<String> = MutableStateFlow(initialSpeaker)
    override val listener: StateFlow<String> = MutableStateFlow(initialListener)
    
    override val onStartRound: () -> Unit = {}
}