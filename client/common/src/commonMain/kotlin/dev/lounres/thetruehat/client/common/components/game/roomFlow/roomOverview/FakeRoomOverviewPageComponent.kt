package dev.lounres.thetruehat.client.common.components.game.roomFlow.roomOverview

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription


public class FakeRoomOverviewPageComponent(
    override val backButtonEnabled: Boolean = true,
    override val roomId: String = "ЗЯНОКУЛЮ",
    userList: List<RoomDescription.Player> =
        listOf(
            RoomDescription.Player(username = "Panther", online = true),
            RoomDescription.Player(username = "Jaguar", online = true),
            RoomDescription.Player(username = "Tiger", online = true),
            RoomDescription.Player(username = "Lion", online = true),
        ),
    playerIndex: Int = 0,
): RoomOverviewPageComponent {
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (language: Language) -> Unit = {}
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}
    override val onSettingsButtonClick: () -> Unit = {}
    override val onRoomIdCopy: () -> Unit = {}
    override val onRoomLinkCopy: () -> Unit = {}

    override val userList: Value<List<RoomDescription.Player>> = MutableValue(userList)
    override val playerIndex: Value<Int> = MutableValue(playerIndex)

    override val onStartGameButtonClick: () -> Unit = {}
}