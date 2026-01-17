package dev.lounres.halfhat.client.ui.implementation.game

import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass
import dev.lounres.halfhat.client.ui.components.game.GamePageComponent
import dev.lounres.halfhat.client.ui.implementation.game.controller.ControllerPageUI
import dev.lounres.halfhat.client.ui.implementation.game.deviceGame.DeviceGamePageUI
import dev.lounres.halfhat.client.ui.implementation.game.localGame.LocalGamePageUI
import dev.lounres.halfhat.client.ui.implementation.game.modeSelection.ModeSelectionPageUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.OnlineGamePageUI
import dev.lounres.halfhat.client.ui.implementation.game.timer.TimerPageUI
import dev.lounres.kone.hub.subscribeAsState


@Composable
public fun GamePageUI(
    component: GamePageComponent,
    windowSizeClass: WindowSizeClass,
) {
    when (val active = component.currentChild.subscribeAsState().value.component) {
        is GamePageComponent.Child.ModeSelection -> ModeSelectionPageUI(active.component, windowSizeClass)
        is GamePageComponent.Child.OnlineGame -> OnlineGamePageUI(active.component, windowSizeClass)
        is GamePageComponent.Child.LocalGame -> LocalGamePageUI(active.component)
        is GamePageComponent.Child.DeviceGame -> DeviceGamePageUI(active.component)
        is GamePageComponent.Child.GameController -> ControllerPageUI(active.component)
        is GamePageComponent.Child.GameTimer -> TimerPageUI(active.component)
    }
}