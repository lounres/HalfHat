package dev.lounres.thetruehat.client.common.components.game.roomEnter

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language


public interface RoomEnterPageComponent {
    public val backButtonEnabled: Boolean
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (language: Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit

    public val roomIdField: Value<String>
    public val onRoomIdChange: (newRoomId: String) -> Unit
    public val onRoomIdPaste: () -> Unit
    public val onRoomIdGenerate: () -> Unit

    public val nicknameField: Value<String>
    public val onNicknameChange: (newNickname: String) -> Unit

    public val onLetsGoButtonClick: () -> Unit
}