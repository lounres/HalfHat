package dev.lounres.halfhat.client.common.ui.components.game.timer

import dev.lounres.halfhat.client.common.logic.components.game.timer.RealTimerComponent
import dev.lounres.halfhat.client.common.logic.components.game.timer.TimerComponent
import dev.lounres.halfhat.client.common.logic.components.game.timer.TimerState
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.logicChildOnRunning
import dev.lounres.kone.state.KoneMutableState
import dev.lounres.kone.state.KoneState
import kotlinx.coroutines.flow.StateFlow


public class RealTimerPageComponent(
    componentContext: UIComponentContext,
    override val onExitTimer: () -> Unit,
    volumeOn: StateFlow<Boolean>,
    initialPreparationTimeSetting: UInt = 3u,
    initialExplanationTimeSetting: UInt = 40u,
    initialLastGuessTimeSetting: UInt = 3u,
) : TimerPageComponent {
    private val timerComponent: TimerComponent =
        RealTimerComponent(
            componentContext = componentContext.logicChildOnRunning(),
            volumeOn = volumeOn,
        )
    
    override val timerState: KoneState<TimerState> get() = timerComponent.timerState
    
    override val preparationTimeSetting: KoneMutableState<UInt> = KoneMutableState(initialPreparationTimeSetting)
    override val explanationTimeSetting: KoneMutableState<UInt> = KoneMutableState(initialExplanationTimeSetting)
    override val lastGuessTimeSetting: KoneMutableState<UInt> = KoneMutableState(initialLastGuessTimeSetting)
    
    override val onStartTimer: () -> Unit = {
        timerComponent.startTimer(
            preparationTimeSetting = preparationTimeSetting.value,
            explanationTimeSetting = explanationTimeSetting.value,
            lastGuessTimeSetting = lastGuessTimeSetting.value,
        )
    }
    override val onResetTimer: () -> Unit = {
        timerComponent.resetTimer()
    }
}