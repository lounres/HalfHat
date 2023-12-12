package dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomOverview

import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription
import kotlinx.coroutines.flow.StateFlow


interface RoomOverviewPageComponent {
    val backButtonEnabled: Boolean
    val onBackButtonClick: () -> Unit
    val onLanguageChange: (language: Language) -> Unit
    val onFeedbackButtonClick: () -> Unit
    val onHatButtonClick: () -> Unit

    val roomId: String
    val userList: StateFlow<List<RoomDescription.Player>?>
    val playerIndex: StateFlow<Int?>
}