package dev.lounres.thetruehat.client.common.components.onlineGame.roundInProgress

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


public class RealRoundInProgressPageComponent(
    private val componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    public override val backButtonEnabled: Boolean,
    public override val unitsUntilEnd: Value<UserGameState.UnitsUntilEnd>,
    public override val showFinishButton: Value<Boolean>,
    public override val volumeOn: Value<Boolean>,
    public override val speakerNickname: Value<String>,
    public override val listenerNickname: Value<String>,
    public override val userRole: Value<RoundInProgressPageComponent.UserRole>,
    private val millisecondsUntilEnd: Value<Long>,
    public override val onBackButtonClick: () -> Unit,
    public override val onLanguageChange: (language: Language) -> Unit,
    public override val onFeedbackButtonClick: () -> Unit,
    public override val onHatButtonClick: () -> Unit,
    public override val onVolumeButtonClick: () -> Unit,
    public override val onFinishButtonClick: () -> Unit,
    public override val onExitButtonClick: () -> Unit,
    public override val onExplainedButtonClick: () -> Unit,
    public override val onNotExplainedButtonClick: () -> Unit,
    public override val onImproperlyExplainedButtonClick: () -> Unit,
): RoundInProgressPageComponent {
    private val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())

    init {
        componentContext.doOnDestroy { coroutineScope.cancel() }
    }

    override val countsUntilEnd: MutableValue<Long> = MutableValue(millisecondsUntilEnd.value / 1000 + 1)
    private var countingDownJob = coroutineScope.launch {
        delay(millisecondsUntilEnd.value % 1000)
        countsUntilEnd.update { it-1 }
        while (countsUntilEnd.value > 0) {
            delay(1000)
            countsUntilEnd.update { it-1 }
        }
    }

    init {
        millisecondsUntilEnd.subscribe { updatedMillisecondsUntilStart ->
            countingDownJob.cancel()
            countsUntilEnd.update { updatedMillisecondsUntilStart / 1000 + 1 }
            countingDownJob = coroutineScope.launch {
                delay(updatedMillisecondsUntilStart % 1000)
                countsUntilEnd.update { it-1 }
                while (countsUntilEnd.value > 0) {
                    delay(1000)
                    countsUntilEnd.update { it-1 }
                }
            }
        }
    }
}