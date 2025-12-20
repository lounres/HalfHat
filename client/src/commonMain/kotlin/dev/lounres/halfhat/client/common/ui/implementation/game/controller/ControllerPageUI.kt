package dev.lounres.halfhat.client.common.ui.implementation.game.controller

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.common.ui.components.game.controller.ControllerPageComponent
import dev.lounres.halfhat.client.common.ui.implementation.game.controller.gameScreen.GameScreenActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.controller.gameScreen.GameScreenUI
import dev.lounres.halfhat.client.common.ui.implementation.game.controller.roomScreen.RoomScreenActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.controller.roomScreen.RoomScreenUI
import dev.lounres.halfhat.client.common.ui.implementation.game.controller.roomSettings.RoomSettingsActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.controller.roomSettings.RoomSettingsUI
import dev.lounres.kone.hub.subscribeAsState


@Composable
public fun RowScope.ControllerPageActionsUI(
    component: ControllerPageComponent,
) {
    when(val active = component.childStack.subscribeAsState().value.active.component) {
        is ControllerPageComponent.Child.RoomScreen -> RoomScreenActionsUI(active.component)
        is ControllerPageComponent.Child.RoomSettings -> RoomSettingsActionsUI(active.component)
        is ControllerPageComponent.Child.GameScreen -> GameScreenActionsUI(active.component)
    }
}

@Composable
public fun ControllerPageUI(
    component: ControllerPageComponent,
) {
    when(val active = component.childStack.subscribeAsState().value.active.component) {
        is ControllerPageComponent.Child.RoomScreen -> RoomScreenUI(active.component)
        is ControllerPageComponent.Child.RoomSettings -> RoomSettingsUI(active.component)
        is ControllerPageComponent.Child.GameScreen -> GameScreenUI(active.component)
    }
}