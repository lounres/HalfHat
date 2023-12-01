package dev.lounres.thetruehat.client.desktop.components.fake

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.desktop.components.RoomPageComponent


class FakeRoomPageComponent(
    backButtonEnabled: Boolean = true,
    override val roomId: String = "ЗЯНОКУЛЮ",
    userList: List<String> = listOf("Panther", "Jaguar", "Tiger", "Lion"),
    playerIndex: Int = 0,
): RoomPageComponent {
    override val theTrueHatPageWithHatComponent = FakeTheTrueHatPageWithHatComponent(
        backButtonEnabled = backButtonEnabled
    )

    override val userList: Value<List<String>> = MutableValue(userList)
    override val playerIndex: Value<Int> = MutableValue(playerIndex)
}