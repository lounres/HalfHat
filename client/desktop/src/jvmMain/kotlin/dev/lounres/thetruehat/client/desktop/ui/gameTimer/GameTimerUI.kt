package dev.lounres.thetruehat.client.desktop.ui.gameTimer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.lounres.thetruehat.client.common.components.gameTimer.GameTimerComponent
import dev.lounres.thetruehat.client.desktop.ui.gameTimer.settings.SettingsPageUI
import dev.lounres.thetruehat.client.desktop.ui.gameTimer.timer.TimerPageUI


@Composable
fun GameTimerUI(
    component: GameTimerComponent
) {
    val slot by component.childSlot.subscribeAsState()
    when(val child = slot.child!!.instance) {
        GameTimerComponent.Child.Settings -> SettingsPageUI(component.settingsPageComponent)
        is GameTimerComponent.Child.Timer -> TimerPageUI(child.timerPageComponent)
    }
}