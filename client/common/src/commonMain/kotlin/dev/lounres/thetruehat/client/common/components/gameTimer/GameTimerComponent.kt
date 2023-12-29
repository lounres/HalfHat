package dev.lounres.thetruehat.client.common.components.gameTimer

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.common.components.gameTimer.settings.SettingsPageComponent
import dev.lounres.thetruehat.client.common.components.gameTimer.timer.TimerPageComponent


public interface GameTimerComponent {
    public val settingsPageComponent: SettingsPageComponent

    public val childSlot: Value<ChildSlot<*, Child>>

    public sealed interface Child {
        public data object Settings: Child
        public data class Timer(val timerPageComponent: TimerPageComponent): Child
    }
}