package dev.lounres.halfhat.client.common.ui.implementation.game.deviceGame

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.DeviceGamePageComponent
import dev.lounres.halfhat.client.common.ui.implementation.game.deviceGame.gameScreen.GameScreenActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.deviceGame.gameScreen.GameScreenUI
import dev.lounres.halfhat.client.common.ui.implementation.game.deviceGame.roomScreen.RoomScreenActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.deviceGame.roomScreen.RoomScreenUI
import dev.lounres.halfhat.client.common.ui.implementation.game.deviceGame.roomSettings.RoomSettingsActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.deviceGame.roomSettings.RoomSettingsUI
import dev.lounres.kone.state.subscribeAsState


@Composable
public fun RowScope.DeviceGamePageActionsUI(
    component: DeviceGamePageComponent,
) {
    when(val active = component.childStack.subscribeAsState().value.active.component) {
        is DeviceGamePageComponent.Child.RoomScreen -> RoomScreenActionsUI(active.component)
        is DeviceGamePageComponent.Child.RoomSettings -> RoomSettingsActionsUI(active.component)
        is DeviceGamePageComponent.Child.GameScreen -> GameScreenActionsUI(active.component)
    }
}

@Composable
public fun DeviceGamePageUI(
    component: DeviceGamePageComponent
) {
    when(val active = component.childStack.subscribeAsState().value.active.component) {
        is DeviceGamePageComponent.Child.RoomScreen -> RoomScreenUI(active.component)
        is DeviceGamePageComponent.Child.RoomSettings -> RoomSettingsUI(active.component)
        is DeviceGamePageComponent.Child.GameScreen -> GameScreenUI(active.component)
    }
}