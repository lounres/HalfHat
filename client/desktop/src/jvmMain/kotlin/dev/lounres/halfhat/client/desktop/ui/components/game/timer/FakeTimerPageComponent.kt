package dev.lounres.halfhat.client.desktop.ui.components.game.timer

import dev.lounres.halfhat.client.desktop.logic.timer.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeTimerPageComponent(
    initialTimerState: State = State.Finished,

    initialPreparationTimeSetting: String = "3",
    initialExplanationTimeSetting: String = "40",
    initialLastGuessTimeSetting: String = "3",
): TimerPageComponent {
    override val onExitTimer: () -> Unit = {}
    
    override val timerState: StateFlow<State> = MutableStateFlow(initialTimerState)

    override val preparationTimeSetting: MutableStateFlow<String> = MutableStateFlow(initialPreparationTimeSetting)
    override val explanationTimeSetting: MutableStateFlow<String> = MutableStateFlow(initialExplanationTimeSetting)
    override val lastGuessTimeSetting: MutableStateFlow<String> = MutableStateFlow(initialLastGuessTimeSetting)

    override val onStartTimer: () -> Unit = {}
    override val onResetTimer: () -> Unit = {}
}