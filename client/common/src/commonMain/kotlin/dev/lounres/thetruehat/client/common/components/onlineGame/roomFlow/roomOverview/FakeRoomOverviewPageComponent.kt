package dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow.roomOverview

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState


public class FakeRoomOverviewPageComponent(
    override val backButtonEnabled: Boolean = true,
    roomId: String = "ЗЯНОКУЛЮ",
    userList: List<UserGameState.Player> =
        listOf(
            UserGameState.Player(username = "Panther", online = true),
            UserGameState.Player(username = "Jaguar", online = true),
            UserGameState.Player(username = "Tiger", online = true),
            UserGameState.Player(username = "Lion", online = true),
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

    override val roomId: Value<String> = MutableValue(roomId)
    override val userList: Value<List<UserGameState.Player>> = MutableValue(userList)
    override val playerIndex: Value<Int> = MutableValue(playerIndex)

    override val onStartGameButtonClick: () -> Unit = {}
}