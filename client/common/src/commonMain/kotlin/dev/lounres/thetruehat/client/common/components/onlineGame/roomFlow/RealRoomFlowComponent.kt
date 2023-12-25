package dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow.roomOverview.RealRoomOverviewPageComponent
import dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow.roomOverview.RoomOverviewPageComponent
import dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow.roomSettings.RealRoomSettingsPageComponent
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.Settings
import dev.lounres.thetruehat.api.models.SettingsUpdate
import dev.lounres.thetruehat.api.models.UserGameState
import dev.lounres.thetruehat.client.common.utils.copyToClipboard
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer


public class RealRoomFlowComponent(
    public val componentContext: ComponentContext,
    public val backButtonEnabled: Boolean,
    public val onBackButtonClick: () -> Unit,
    public val onLanguageChange: (language: Language) -> Unit,
    public val onFeedbackButtonClick: () -> Unit,
    public val onHatButtonClick: () -> Unit,
    public val roomId: MutableValue<String>,
    public val availableDictionaries: Value<List<UserGameState.ServerDictionary>>,
    public val userList: MutableValue<List<UserGameState.Player>>,
    public val playerIndex: MutableValue<Int>,
    public val settings: MutableValue<Settings>,
    public val onApplySettings: (SettingsUpdate) -> Unit,
    public val onStartGame: () -> Unit,
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
            onRoomIdCopy = { copyToClipboard(roomId.value) },
            onRoomLinkCopy = { copyToClipboard(TODO()) },
            onStartGameButtonClick = onStartGame
        )

    public val navigation: SlotNavigation<ChildConfiguration> = SlotNavigation()

    override public val childSlot: Value<ChildSlot<ChildConfiguration, RoomFlowComponent.Child>> =
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

                            availableDictionaries = availableDictionaries,

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
    public sealed interface ChildConfiguration {
        @Serializable
        public data object Overview: ChildConfiguration
        @Serializable
        public data object Settings: ChildConfiguration
    }
}