package dev.lounres.halfhat.client.common.ui.components.game.timer

import dev.lounres.halfhat.client.common.logic.components.game.timer.RealTimerComponent
import dev.lounres.halfhat.client.common.logic.components.game.timer.TimerComponent
import dev.lounres.halfhat.client.common.logic.components.game.timer.TimerState
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildLogicChildOnRunning
import dev.lounres.kone.state.KoneMutableState
import dev.lounres.kone.state.KoneState
import kotlinx.coroutines.flow.StateFlow


public class RealTimerPageComponent(
    override val onExitTimer: () -> Unit,
    initialPreparationTimeSetting: UInt = 3u,
    initialExplanationTimeSetting: UInt = 40u,
    initialLastGuessTimeSetting: UInt = 3u,
    private val timerComponent: TimerComponent
) : TimerPageComponent {
    
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

public suspend fun RealTimerPageComponent(
    componentContext: UIComponentContext,
    onExitTimer: () -> Unit,
    volumeOn: StateFlow<Boolean>,
    initialPreparationTimeSetting: UInt,
    initialExplanationTimeSetting: UInt,
    initialLastGuessTimeSetting: UInt,
): RealTimerPageComponent {
    
    val timerComponent: TimerComponent =
        componentContext.buildLogicChildOnRunning {
            RealTimerComponent(
                componentContext = it,
                volumeOn = volumeOn,
            )
        }
    
    return RealTimerPageComponent(
        onExitTimer = onExitTimer,
        initialPreparationTimeSetting = initialPreparationTimeSetting,
        initialExplanationTimeSetting = initialExplanationTimeSetting,
        initialLastGuessTimeSetting = initialLastGuessTimeSetting,
        timerComponent = timerComponent
    )
}