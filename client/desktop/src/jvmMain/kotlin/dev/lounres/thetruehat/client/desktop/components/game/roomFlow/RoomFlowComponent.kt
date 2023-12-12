package dev.lounres.thetruehat.client.desktop.components.game.roomFlow

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomSettings.RoomSettingsPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomOverview.RoomOverviewPageComponent


interface RoomFlowComponent {
    val roomOverviewPageComponent: RoomOverviewPageComponent

    val childSlot: Value<ChildSlot<*, Child>>

    sealed interface Child {
        data object Overview: Child
        data class Settings(val roomSettingsPageComponent: RoomSettingsPageComponent): Child
    }
}