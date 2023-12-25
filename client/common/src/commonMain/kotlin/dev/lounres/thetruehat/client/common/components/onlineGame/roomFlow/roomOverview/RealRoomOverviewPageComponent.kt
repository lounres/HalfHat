package dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow.roomOverview

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState


public class RealRoomOverviewPageComponent(
    override val backButtonEnabled: Boolean,
    override val onBackButtonClick: () -> Unit,
    override val onLanguageChange: (language: Language) -> Unit,
    override val onFeedbackButtonClick: () -> Unit,
    override val onHatButtonClick: () -> Unit,
    override val onSettingsButtonClick: () -> Unit,
    override val onRoomIdCopy: () -> Unit,
    override val onRoomLinkCopy: () -> Unit,
    override val roomId: Value<String>,
    override val userList: Value<List<UserGameState.Player>>,
    override val playerIndex: Value<Int>,
    override val onStartGameButtonClick: () -> Unit,
): RoomOverviewPageComponent