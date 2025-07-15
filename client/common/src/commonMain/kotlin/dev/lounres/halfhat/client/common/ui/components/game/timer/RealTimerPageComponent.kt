package dev.lounres.halfhat.client.common.ui.components.game.timer

import dev.lounres.halfhat.client.common.logic.components.game.timer.RealTimerComponent
import dev.lounres.halfhat.client.common.logic.components.game.timer.TimerComponent
import dev.lounres.halfhat.client.common.logic.components.game.timer.TimerState
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildLogicChildOnRunning
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.KoneMutableBlockingHub
import dev.lounres.kone.hub.value
import dev.lounres.kone.relations.defaultEquality
import kotlinx.coroutines.flow.StateFlow


public class RealTimerPageComponent(
    override val onExitTimer: () -> Unit,
    initialPreparationTimeSetting: UInt,
    initialExplanationTimeSetting: UInt,
    initialLastGuessTimeSetting: UInt,
    private val timerComponent: TimerComponent
) : TimerPageComponent {
    
    override val timerState: KoneAsynchronousHub<TimerState> get() = timerComponent.timerState
    
    override val preparationTimeSetting: KoneMutableBlockingHub<String> = KoneMutableBlockingHub(initialPreparationTimeSetting.toString(), defaultEquality() /* FIXME: Remove the default value */)
    override val explanationTimeSetting: KoneMutableBlockingHub<String> = KoneMutableBlockingHub(initialExplanationTimeSetting.toString(), defaultEquality() /* FIXME: Remove the default value */)
    override val lastGuessTimeSetting: KoneMutableBlockingHub<String> = KoneMutableBlockingHub(initialLastGuessTimeSetting.toString(), defaultEquality() /* FIXME: Remove the default value */)
    
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