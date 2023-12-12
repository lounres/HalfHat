package dev.lounres.thetruehat.client.desktop.components.game.roomEnter

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language


interface RoomEnterPageComponent {
    val backButtonEnabled: Boolean
    val onBackButtonClick: () -> Unit
    val onLanguageChange: (language: Language) -> Unit
    val onFeedbackButtonClick: () -> Unit
    val onHatButtonClick: () -> Unit

    val roomIdField: Value<String>
    val onRoomIdChange: (newRoomId: String) -> Unit
    val onRoomIdPaste: () -> Unit
    val onRoomIdGenerate: () -> Unit

    val nicknameField: Value<String>
    val onNicknameChange: (newNickname: String) -> Unit

    val onLetsGoButtonClick: () -> Unit
}