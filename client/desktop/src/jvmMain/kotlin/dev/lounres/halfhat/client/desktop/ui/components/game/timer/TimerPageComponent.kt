package dev.lounres.halfhat.client.desktop.ui.components.game.timer

import dev.lounres.halfhat.client.desktop.logic.timer.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


interface TimerPageComponent {
    val onExitTimer: () -> Unit
    
    val timerState: StateFlow<State>

    val preparationTimeSetting: MutableStateFlow<String>
    val explanationTimeSetting: MutableStateFlow<String>
    val lastGuessTimeSetting: MutableStateFlow<String>
    
    val onStartTimer: () -> Unit
    val onResetTimer: () -> Unit
}