package dev.lounres.thetruehat.client.desktop.ui.onlineGame.roomFlow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow.RoomFlowComponent
import dev.lounres.thetruehat.client.desktop.ui.onlineGame.roomFlow.roomOverview.RoomOverviewPageUI
import dev.lounres.thetruehat.client.desktop.ui.onlineGame.roomFlow.roomSettings.RoomSettingsPageUI


@Composable
public fun RoomFlowUI(
    component: RoomFlowComponent
) {
    val childSlot by component.childSlot.subscribeAsState()
    when(val child = childSlot.child!!.instance) {
        RoomFlowComponent.Child.Overview -> RoomOverviewPageUI(component.roomOverviewPageComponent)
        is RoomFlowComponent.Child.Settings -> RoomSettingsPageUI(child.roomSettingsPageComponent)
    }
}