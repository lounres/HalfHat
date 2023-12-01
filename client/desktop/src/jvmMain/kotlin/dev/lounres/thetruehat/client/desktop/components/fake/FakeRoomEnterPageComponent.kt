package dev.lounres.thetruehat.client.desktop.components.fake

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.desktop.components.RoomEnterPageComponent


class FakeRoomEnterPageComponent(
    backButtonEnabled: Boolean = true,
    roomId: String = "",
    nickname: String = "",
): RoomEnterPageComponent {
    override val theTrueHatPageWithHatComponent = FakeTheTrueHatPageWithHatComponent(
        backButtonEnabled = backButtonEnabled,
    )

    override val roomId: Value<String> = MutableValue(roomId)
    override fun onRoomIdChange(newRoomId: String) {}

    override val nickname: Value<String> = MutableValue(nickname)
    override fun onNicknameChange(newNickname: String) {}
}