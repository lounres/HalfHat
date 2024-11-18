package dev.lounres.halfhat.client.desktop.ui.implementation.game

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.lounres.halfhat.client.desktop.ui.components.game.GamePageComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.DeviceGamePageActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.DeviceGamePageUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.localGame.LocalGamePageActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.localGame.LocalGamePageUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.modeSelection.ModeSelectionPageUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.OnlineGamePageActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.OnlineGamePageUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.timer.TimerPageActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.timer.TimerPageUI


@Composable
fun RowScope.GamePageActionsUI(
    component: GamePageComponent,
) {
    when (val active = component.childStack.subscribeAsState().value.active.instance) {
        is GamePageComponent.Child.ModeSelection -> {}
        is GamePageComponent.Child.OnlineGame -> OnlineGamePageActionsUI(active.component)
        is GamePageComponent.Child.LocalGame -> LocalGamePageActionsUI(active.component)
        is GamePageComponent.Child.DeviceGame -> DeviceGamePageActionsUI(active.component)
        is GamePageComponent.Child.GameTimer -> TimerPageActionsUI(active.component)
    }
}

@Composable
fun GamePageUI(
    component: GamePageComponent,
) {
    when (val active = component.childStack.subscribeAsState().value.active.instance) {
        is GamePageComponent.Child.ModeSelection -> ModeSelectionPageUI(active.component)
        is GamePageComponent.Child.OnlineGame -> OnlineGamePageUI(active.component)
        is GamePageComponent.Child.LocalGame -> LocalGamePageUI(active.component)
        is GamePageComponent.Child.DeviceGame -> DeviceGamePageUI(active.component)
        is GamePageComponent.Child.GameTimer -> TimerPageUI(active.component)
    }
}