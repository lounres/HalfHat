package dev.lounres.thetruehat.client.desktop.components.game.roomEnter

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.client.common.utils.copyFromClipboard


class RealRoomEnterPageComponent(
    override val backButtonEnabled: Boolean,
    override val onBackButtonClick: () -> Unit,
    override val onLanguageChange: (language: Language) -> Unit,
    override val onFeedbackButtonClick: () -> Unit,
    override val onHatButtonClick: () -> Unit,
    generateRoomId: () -> Unit,
    roomIdFieldInitialValue: String = "",
    onNicknameChangeInitialValue: String = "",
    onLetsGoAction: (roomId: String, nickname: String) -> Unit,
): RoomEnterPageComponent {
    override val roomIdField: MutableValue<String> = MutableValue(roomIdFieldInitialValue)
    override val onRoomIdChange: (newRoomId: String) -> Unit = { newRoomId -> roomIdField.update { newRoomId } }

    override val onRoomIdPaste: () -> Unit = { roomIdField.update { copyFromClipboard() } }
    override val onRoomIdGenerate: () -> Unit = generateRoomId

    override val nicknameField: MutableValue<String> = MutableValue(onNicknameChangeInitialValue)
    override val onNicknameChange: (newNickname: String) -> Unit = { newNickname -> nicknameField.update { newNickname } }

    override val onLetsGoButtonClick: () -> Unit = { onLetsGoAction(roomIdField.value, nicknameField.value) }
}