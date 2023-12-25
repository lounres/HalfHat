package dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow.roomOverview.RoomOverviewPageComponent
import dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow.roomSettings.RoomSettingsPageComponent


public interface RoomFlowComponent {
    public val roomOverviewPageComponent: RoomOverviewPageComponent

    public val childSlot: Value<ChildSlot<*, Child>>

    public sealed interface Child {
        public data object Overview: Child
        public data class Settings(val roomSettingsPageComponent: RoomSettingsPageComponent): Child
    }
}