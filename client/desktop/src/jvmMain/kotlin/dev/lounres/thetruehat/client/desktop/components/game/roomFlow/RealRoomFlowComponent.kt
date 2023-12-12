package dev.lounres.thetruehat.client.desktop.components.game.roomFlow

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription
import dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomOverview.RealRoomOverviewPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomOverview.RoomOverviewPageComponent
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer


class RealRoomFlowComponent(
    val componentContext: ComponentContext,
    val backButtonEnabled: Boolean,
    val onBackButtonClick: () -> Unit,
    val onLanguageChange: (language: Language) -> Unit,
    val onFeedbackButtonClick: () -> Unit,
    val onHatButtonClick: () -> Unit,
    val roomId: String,
    val userList: StateFlow<List<RoomDescription.Player>?>,
    val playerIndex: StateFlow<Int?>,
): RoomFlowComponent {
    override val roomOverviewPageComponent: RoomOverviewPageComponent =
        RealRoomOverviewPageComponent(
            backButtonEnabled = backButtonEnabled,
            onBackButtonClick = onBackButtonClick,
            onLanguageChange = onLanguageChange,
            onFeedbackButtonClick = onFeedbackButtonClick,
            onHatButtonClick = onHatButtonClick,
            roomId = roomId,
            userList = userList,
            playerIndex = playerIndex,
        )

    val navigation = SlotNavigation<ChildConfiguration>()

    override val childSlot: Value<ChildSlot<ChildConfiguration, RoomFlowComponent.Child>> =
        componentContext.childSlot(
            source = navigation,
            serializer = serializer<ChildConfiguration>(),
            initialConfiguration = { ChildConfiguration.Overview },
        ) { configuration, componentContext ->
            when(configuration) {
                ChildConfiguration.Overview -> RoomFlowComponent.Child.Overview
                ChildConfiguration.Settings -> TODO()
            }
        }

    @Serializable
    sealed interface ChildConfiguration {
        @Serializable
        data object Overview: ChildConfiguration
        @Serializable
        data object Settings: ChildConfiguration
    }
}