package dev.lounres.thetruehat.client.desktop.components

import com.arkivanov.decompose.value.Value


interface RoomEnterPageComponent {
    val theTrueHatPageWithHatComponent: TheTrueHatPageWithHatComponent

    val roomId: Value<String>
    fun onRoomIdChange(newRoomId: String)

    val nickname: Value<String>
    fun onNicknameChange(newNickname: String)
}