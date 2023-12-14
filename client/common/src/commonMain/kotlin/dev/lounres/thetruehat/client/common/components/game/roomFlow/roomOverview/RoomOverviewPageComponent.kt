package dev.lounres.thetruehat.client.common.components.game.roomFlow.roomOverview

import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription
import kotlinx.coroutines.flow.StateFlow


public interface RoomOverviewPageComponent {
    public val backButtonEnabled: Boolean
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (language: Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit
    public val onSettingsButtonClick: () -> Unit
    public val onRoomIdCopy: () -> Unit
    public val onRoomLinkCopy: () -> Unit

    public val roomId: String
    public val userList: StateFlow<List<RoomDescription.Player>?>
    public val playerIndex: StateFlow<Int?>

    public val onStartGameButtonClick: () -> Unit
}