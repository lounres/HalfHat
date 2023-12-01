package dev.lounres.thetruehat.client.desktop.components

import com.arkivanov.decompose.value.Value


interface RoomPageComponent {
    val theTrueHatPageWithHatComponent: TheTrueHatPageWithHatComponent

    val roomId: String
    val userList: Value<List<String>>
    val playerIndex: Value<Int>
}