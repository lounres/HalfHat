package dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow.roomOverview

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState


public interface RoomOverviewPageComponent {
    public val backButtonEnabled: Boolean
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (language: Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit
    public val onSettingsButtonClick: () -> Unit
    public val onRoomIdCopy: () -> Unit
    public val onRoomLinkCopy: () -> Unit

    public val roomId: Value<String>
    public val userList: Value<List<UserGameState.Player>>
    public val playerIndex: Value<Int>

    public val onStartGameButtonClick: () -> Unit
}