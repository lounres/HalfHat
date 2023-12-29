package dev.lounres.thetruehat.client.common.components.onlineGame.roundCountdown

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState
import dev.lounres.thetruehat.client.common.utils.playSound
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


public class RealRoundCountdownPageComponent(
    private val componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    override val backButtonEnabled: Boolean,
    override val unitsUntilEnd: Value<UserGameState.UnitsUntilEnd>,
    override val volumeOn: Value<Boolean>,
    override val showFinishButton: Value<Boolean>,
    private val millisecondsUntilStart: Value<Long>,
    override val onBackButtonClick: () -> Unit,
    override val onLanguageChange: (Language) -> Unit,
    override val onFeedbackButtonClick: () -> Unit,
    override val onHatButtonClick: () -> Unit,
    override val onExitButtonClick: () -> Unit,
    override val onVolumeButtonClick: () -> Unit,
    override val onFinishButtonClick: () -> Unit,
): RoundCountdownPageComponent {
    private val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())

    init {
        componentContext.doOnDestroy { coroutineScope.cancel() }
    }

    override val countsUntilStart: MutableValue<Long> = MutableValue(millisecondsUntilStart.value / 1000 + 1)
    private var countingDownJob = coroutineScope.launch {
        delay(millisecondsUntilStart.value % 1000)
        if (volumeOn.value) playSound("/sounds/countdown.wav")
        countsUntilStart.update { it-1 }
        while (countsUntilStart.value > 0) {
            delay(1000)
            if (volumeOn.value) playSound("/sounds/countdown.wav")
            countsUntilStart.update { it-1 }
        }
    }

    init {
        millisecondsUntilStart.subscribe { updatedMillisecondsUntilStart ->
            countingDownJob.cancel()
            countsUntilStart.update { updatedMillisecondsUntilStart / 1000 + 1 }
            countingDownJob = coroutineScope.launch {
                delay(updatedMillisecondsUntilStart % 1000)
                if (volumeOn.value) playSound("/sounds/countdown.wav")
                countsUntilStart.update { it-1 }
                while (countsUntilStart.value > 0) {
                    delay(1000)
                    if (volumeOn.value) playSound("/sounds/countdown.wav")
                    countsUntilStart.update { it-1 }
                }
            }
        }
    }
}