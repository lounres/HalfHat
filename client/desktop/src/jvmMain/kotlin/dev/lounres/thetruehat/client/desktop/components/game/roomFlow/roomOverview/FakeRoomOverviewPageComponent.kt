package dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomOverview

import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeRoomOverviewPageComponent(
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

    override val userList: StateFlow<List<RoomDescription.Player>?> = MutableStateFlow(userList)
    override val playerIndex: StateFlow<Int?> = MutableStateFlow(playerIndex)
}