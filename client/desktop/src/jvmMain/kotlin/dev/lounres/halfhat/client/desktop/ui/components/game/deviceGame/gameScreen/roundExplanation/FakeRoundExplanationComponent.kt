package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundExplanation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeRoundExplanationComponent(
    initialSpeaker: String = "Tiger",
    initialListener: String = "Panther",
    millisecondsLeft: UInt = 29_000u,
    initialWord: String = "мышеловка",
) : RoundExplanationComponent {
    override val onExitGame: () -> Unit = {}
    
    override val speaker: StateFlow<String> = MutableStateFlow(initialSpeaker)
    override val listener: StateFlow<String> = MutableStateFlow(initialListener)
    override val millisecondsLeft: StateFlow<UInt> = MutableStateFlow(millisecondsLeft)
    override val word: StateFlow<String> = MutableStateFlow(initialWord)
    
    override val onGuessed: () -> Unit = {}
    override val onNotGuessed: () -> Unit = {}
    override val onMistake: () -> Unit = {}
}