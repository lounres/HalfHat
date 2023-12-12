package dev.lounres.thetruehat.client.desktop.components.game.roomEnter

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language


class FakeRoomEnterPageComponent(
    override val backButtonEnabled: Boolean = true,
    roomId: String = "",
    nickname: String = "",
): RoomEnterPageComponent {
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (language: Language) -> Unit = {}
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}

    override val roomIdField: Value<String> = MutableValue(roomId)
    override val onRoomIdChange: (newRoomId: String) -> Unit = {}
    override val onRoomIdPaste: () -> Unit = {}
    override val onRoomIdGenerate: () -> Unit = {}

    override val nicknameField: Value<String> = MutableValue(nickname)
    override val onNicknameChange: (newNickname: String) -> Unit = {}

    override val onLetsGoButtonClick: () -> Unit = {}
}