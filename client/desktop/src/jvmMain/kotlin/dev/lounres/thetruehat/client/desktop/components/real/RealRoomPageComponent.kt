package dev.lounres.thetruehat.client.desktop.components.real

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.desktop.components.Language
import dev.lounres.thetruehat.client.desktop.components.RoomPageComponent
import dev.lounres.thetruehat.client.desktop.components.TheTrueHatPageWithHatComponent


inline fun RealRoomPageComponent(
    backButtonEnabled: Boolean,
    crossinline onBackButtonClick: () -> Unit,
    crossinline onLanguageChange: (language: Language) -> Unit,
    crossinline onFeedbackButtonClick: () -> Unit,
    crossinline onHatButtonClick: () -> Unit,
    roomId: String,
    userList: Value<List<String>>,
    playerIndex: Value<Int>,
): RealRoomPageComponent =
    object : RealRoomPageComponent(
        roomId = roomId,
        userList = userList,
        playerIndex = playerIndex,
    ) {
        override val theTrueHatPageWithHatComponent: TheTrueHatPageWithHatComponent =
            RealTheTrueHatPageWithHatComponent(
                backButtonEnabled = backButtonEnabled,
                onBackButtonClick = onBackButtonClick,
                onLanguageChange = onLanguageChange,
                onFeedbackButtonClick = onFeedbackButtonClick,
                onHatButtonClick = onHatButtonClick,
            )
    }

abstract class RealRoomPageComponent(
    override val roomId: String,
    override val userList: Value<List<String>>,
    override val playerIndex: Value<Int>,
): RoomPageComponent