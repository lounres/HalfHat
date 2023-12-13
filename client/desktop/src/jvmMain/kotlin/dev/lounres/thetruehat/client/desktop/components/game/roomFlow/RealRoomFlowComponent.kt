package dev.lounres.thetruehat.client.desktop.components.game.roomFlow

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription
import dev.lounres.thetruehat.api.models.Settings
import dev.lounres.thetruehat.api.models.SettingsUpdate
import dev.lounres.thetruehat.client.common.utils.copyToClipboard
import dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomOverview.RealRoomOverviewPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomOverview.RoomOverviewPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomSettings.RealRoomSettingsPageComponent
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
    val settings: Value<Settings>,
    val onApplySettings: (SettingsUpdate) -> Unit,
    val onStartGame: () -> Unit,
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
            onSettingsButtonClick = { navigation.activate(ChildConfiguration.Settings) },
            onRoomIdCopy = { copyToClipboard(roomId) },
            onRoomLinkCopy = { copyToClipboard(TODO()) },
            onStartGameButtonClick = onStartGame
        )

    val navigation = SlotNavigation<ChildConfiguration>()

    override val childSlot: Value<ChildSlot<ChildConfiguration, RoomFlowComponent.Child>> =
        componentContext.childSlot(
            source = navigation,
            serializer = serializer<ChildConfiguration>(),
            initialConfiguration = { ChildConfiguration.Overview },
        ) { configuration, _ ->
            when(configuration) {
                ChildConfiguration.Overview -> RoomFlowComponent.Child.Overview
                ChildConfiguration.Settings ->
                    RoomFlowComponent.Child.Settings(
                        roomSettingsPageComponent = RealRoomSettingsPageComponent(
                            backButtonEnabled = true,
                            onBackButtonClick = { navigation.activate(ChildConfiguration.Overview) },
                            onLanguageChange = onLanguageChange,
                            onFeedbackButtonClick = onFeedbackButtonClick,
                            onHatButtonClick = onHatButtonClick,

                            settings = settings.value,
                            onApplySettings = {
                                onApplySettings(it)
                                navigation.activate(ChildConfiguration.Overview)
                            },
                            onCancelButtonClick = { navigation.activate(ChildConfiguration.Overview) },
                        )
                    )
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