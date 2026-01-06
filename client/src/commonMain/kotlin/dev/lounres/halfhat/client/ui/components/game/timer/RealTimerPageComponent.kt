package dev.lounres.halfhat.client.ui.components.game.timer

import dev.lounres.halfhat.client.logic.components.game.timer.RealTimerComponent
import dev.lounres.halfhat.client.logic.components.game.timer.TimerComponent
import dev.lounres.halfhat.client.logic.components.game.timer.TimerState
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildLogicChildOnRunning
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHub


public class RealTimerPageComponent(
    override val onExitTimer: () -> Unit,
    initialPreparationTimeSetting: UInt,
    initialExplanationTimeSetting: UInt,
    initialLastGuessTimeSetting: UInt,
    private val timerComponent: TimerComponent
) : TimerPageComponent {
    
    override val timerState: KoneAsynchronousHub<TimerState> get() = timerComponent.timerState
    
    override val preparationTimeSetting: KoneMutableAsynchronousHub<String> = KoneMutableAsynchronousHub(initialPreparationTimeSetting.toString())
    override val explanationTimeSetting: KoneMutableAsynchronousHub<String> = KoneMutableAsynchronousHub(initialExplanationTimeSetting.toString())
    override val lastGuessTimeSetting: KoneMutableAsynchronousHub<String> = KoneMutableAsynchronousHub(initialLastGuessTimeSetting.toString())
    
    override val onStartTimer: () -> Unit = onStartTimer@{
        timerComponent.startTimer(
            preparationTimeSetting = preparationTimeSetting.value.let { if (it.isBlank()) return@onStartTimer else it.toUInt() },
            explanationTimeSetting = explanationTimeSetting.value.let { if (it.isBlank()) return@onStartTimer else it.toUInt() },
            lastGuessTimeSetting = lastGuessTimeSetting.value.let { if (it.isBlank()) return@onStartTimer else it.toUInt() },
        )
    }
    override val onResetTimer: () -> Unit = {
        timerComponent.resetTimer()
    }
}

public suspend fun RealTimerPageComponent(
    componentContext: UIComponentContext,
    onExitTimer: () -> Unit,
    initialPreparationTimeSetting: UInt,
    initialExplanationTimeSetting: UInt,
    initialLastGuessTimeSetting: UInt,
): RealTimerPageComponent {
    
    val timerComponent: TimerComponent =
        componentContext.buildLogicChildOnRunning {
            RealTimerComponent(
                componentContext = it,
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