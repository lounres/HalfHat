package dev.lounres.thetruehat.client.common.components.gameTimer.timer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.client.common.utils.playSound
import dev.lounres.thetruehat.client.common.utils.runOnUiThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


public class RealTimerPageComponent(
    private val componentContext: ComponentContext,
    private val coroutineContext: CoroutineContext,
    override val backButtonEnabled: Boolean,
    override val onBackButtonClick: () -> Unit,
    override val onLanguageChange: (Language) -> Unit,
    override val onFeedbackButtonClick: () -> Unit,
    override val onHatButtonClick: () -> Unit,

    public val countdownTime: Int,
    public val explanationTime: Int,
    public val finalGuessTime: Int,

    override val onResetButtonClick: () -> Unit
) : TimerPageComponent {
    private val coroutineScope = CoroutineScope(coroutineContext)

    init {
        componentContext.doOnDestroy(coroutineScope::cancel)
    }

    override val timeLeft: MutableValue<TimerPageComponent.TimerEntry> = MutableValue(TimerPageComponent.TimerEntry.Countdown(countdownTime))

    init {
        coroutineScope.launch {
            while (true) {
                when (val time = timeLeft.value) {
                    is TimerPageComponent.TimerEntry.Countdown -> {
                        playSound("/sounds/countdown.wav")
                        delay(1000)
                        timeLeft.update {
                            if (time.timeLeft > 1) TimerPageComponent.TimerEntry.Countdown(time.timeLeft - 1)
                            else {
                                playSound("/sounds/explanationStart.wav")
                                TimerPageComponent.TimerEntry.Explanation(explanationTime)
                            }
                        }
                    }
                    is TimerPageComponent.TimerEntry.Explanation -> {
                        delay(1000)
                        timeLeft.update {
                            if (time.timeLeft > 1) TimerPageComponent.TimerEntry.Explanation(time.timeLeft - 1)
                            else {
                                playSound("/sounds/finalGuessStart.wav")
                                TimerPageComponent.TimerEntry.FinalGuess(finalGuessTime * 10)
                            }
                        }
                    }
                    is TimerPageComponent.TimerEntry.FinalGuess -> {
                        delay(100)
                        if (time.timeLeft > 1) {
                            timeLeft.update { TimerPageComponent.TimerEntry.FinalGuess(time.timeLeft - 1) }
                        } else {
                            playSound("/sounds/finalGuessEnd.wav")
                            runOnUiThread {
                                onResetButtonClick()
                            }
                            break
                        }
                    }
                }
            }
        }
    }
}