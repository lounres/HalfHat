package dev.lounres.halfhat.client.common.ui.components.game.controller.gameScreen

import dev.lounres.halfhat.client.common.logic.components.game.timer.RealTimerComponent
import dev.lounres.halfhat.client.common.logic.components.game.timer.TimerComponent
import dev.lounres.halfhat.client.common.logic.components.game.timer.TimerState
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildLogicChildOnRunning
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.map
import dev.lounres.kone.hub.subscribe
import dev.lounres.kone.hub.update
import kotlinx.coroutines.flow.StateFlow


public class RealGameScreenComponent(
    override val onExitGameController: () -> Unit,
    
    override val timerState: KoneAsynchronousHub<TimerState>,
    override val speaker: KoneAsynchronousHub<String>,
    override val listener: KoneAsynchronousHub<String>,
    
    override val onStartTimer: () -> Unit,
    override val onFinishTimer: () -> Unit,
) : GameScreenComponent

private data class SpeakerAndListenerIndex(
    val speakerIndex: UInt,
    val listenerIndex: UInt,
)

public suspend fun RealGameScreenComponent(
    componentContext: UIComponentContext,
    volumeOn: StateFlow<Boolean>,
    playersList: KoneList<String>,
    preparationTimeSeconds: UInt,
    explanationTimeSeconds: UInt,
    finalGuessTimeSeconds: UInt,
    onExitGameController: () -> Unit,
): RealGameScreenComponent {
    val timerComponent: TimerComponent =
        componentContext.buildLogicChildOnRunning {
            RealTimerComponent(
                componentContext = it,
                volumeOn = volumeOn,
            )
        }
    
    val speakerAndListenerIndices = KoneMutableAsynchronousHub(SpeakerAndListenerIndex(0u, 1u))
    
    timerComponent.timerState.subscribe { timerState ->
        if (timerState is TimerState.Finished)
            speakerAndListenerIndices.update {
                val nextSpeakerIndex = (it.speakerIndex + 1u) % playersList.size
                var nextListenerIndex = (it.listenerIndex + 1u) % playersList.size
                if (nextSpeakerIndex == 0u) {
                    nextListenerIndex = (nextListenerIndex + 1u) % playersList.size
                    if (nextListenerIndex == 0u) {
                        nextListenerIndex = 1u
                    }
                }
                SpeakerAndListenerIndex(nextSpeakerIndex, nextListenerIndex)
            }
    }
    
    return RealGameScreenComponent(
        onExitGameController = onExitGameController,
        
        timerState = timerComponent.timerState,
        speaker = speakerAndListenerIndices.map { playersList[it.speakerIndex] },
        listener = speakerAndListenerIndices.map { playersList[it.listenerIndex] },
        
        onStartTimer = {
            timerComponent.startTimer(
                preparationTimeSetting = preparationTimeSeconds,
                explanationTimeSetting = explanationTimeSeconds,
                lastGuessTimeSetting = finalGuessTimeSeconds,
            )
        },
        onFinishTimer = { timerComponent.resetTimer() },
    )
}