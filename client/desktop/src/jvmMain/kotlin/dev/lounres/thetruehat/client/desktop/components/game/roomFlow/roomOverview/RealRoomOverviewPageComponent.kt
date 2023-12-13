package dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomOverview

import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription
import kotlinx.coroutines.flow.StateFlow


class RealRoomOverviewPageComponent(
    override val backButtonEnabled: Boolean,
    override val onBackButtonClick: () -> Unit,
    override val onLanguageChange: (language: Language) -> Unit,
    override val onFeedbackButtonClick: () -> Unit,
    override val onHatButtonClick: () -> Unit,
    override val onSettingsButtonClick: () -> Unit,
    override val onRoomIdCopy: () -> Unit,
    override val onRoomLinkCopy: () -> Unit,
    override val roomId: String,
    override val userList: StateFlow<List<RoomDescription.Player>?>,
    override val playerIndex: StateFlow<Int?>,
    override val onStartGameButtonClick: () -> Unit,
): RoomOverviewPageComponent