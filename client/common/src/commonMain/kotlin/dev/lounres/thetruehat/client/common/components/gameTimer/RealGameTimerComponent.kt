package dev.lounres.thetruehat.client.common.components.gameTimer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.client.common.components.gameTimer.settings.RealSettingsPageComponent
import dev.lounres.thetruehat.client.common.components.gameTimer.settings.SettingsPageComponent
import dev.lounres.thetruehat.client.common.components.gameTimer.timer.RealTimerPageComponent
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlin.coroutines.CoroutineContext


public class RealGameTimerComponent(
    private val componentContext: ComponentContext,
    private val coroutineContext: CoroutineContext,
    private val backButtonEnabled: Boolean,
    private val onBackButtonClick: () -> Unit,
    private val onLanguageChange: (Language) -> Unit,
    private val onFeedbackButtonClick: () -> Unit,
    private val onHatButtonClick: () -> Unit,
): GameTimerComponent {
    override val settingsPageComponent: SettingsPageComponent = RealSettingsPageComponent(
        backButtonEnabled = backButtonEnabled,
        onBackButtonClick = onBackButtonClick,
        onLanguageChange = onLanguageChange,
        onFeedbackButtonClick = onFeedbackButtonClick,
        onHatButtonClick = onHatButtonClick,
        onStart = { countdownTime, explanationTime, finalGuessTime ->
            navigation.activate(
                ChildConfiguration.Timer(
                    countdownTime = countdownTime,
                    explanationTime = explanationTime,
                    finalGuessTime = finalGuessTime,
                )
            )
        }
    )

    private val navigation = SlotNavigation<ChildConfiguration>()

    override val childSlot: Value<ChildSlot<ChildConfiguration, GameTimerComponent.Child>> =
        componentContext.childSlot(
            source = navigation,
            serializer = serializer<ChildConfiguration>(),
            initialConfiguration = { ChildConfiguration.Settings },
        ) { configuration, componentContext ->
            when(configuration) {
                ChildConfiguration.Settings -> GameTimerComponent.Child.Settings
                is ChildConfiguration.Timer ->
                    GameTimerComponent.Child.Timer(
                        timerPageComponent = RealTimerPageComponent(
                            componentContext = componentContext,
                            coroutineContext = coroutineContext,
                            backButtonEnabled = true,
                            onBackButtonClick = { navigation.activate(ChildConfiguration.Settings) },
                            onLanguageChange = onLanguageChange,
                            onFeedbackButtonClick = onFeedbackButtonClick,
                            onHatButtonClick = onHatButtonClick,
                            countdownTime = configuration.countdownTime,
                            explanationTime = configuration.explanationTime,
                            finalGuessTime = configuration.finalGuessTime,
                            onResetButtonClick = { navigation.activate(ChildConfiguration.Settings) }
                        )
                    )
            }
        }

    @Serializable
    public sealed interface ChildConfiguration {
        @Serializable
        public data object Settings: ChildConfiguration
        @Serializable
        public data class Timer(
            val countdownTime: Int,
            val explanationTime: Int,
            val finalGuessTime: Int,
        ): ChildConfiguration
    }
}