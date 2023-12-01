package dev.lounres.thetruehat.client.desktop.components.real

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.desktop.components.Language
import dev.lounres.thetruehat.client.desktop.components.RoomEnterPageComponent
import dev.lounres.thetruehat.client.desktop.components.TheTrueHatPageWithHatComponent


inline fun RealRoomEnterPageComponent(
    backButtonEnabled: Boolean,
    crossinline onBackButtonClick: () -> Unit,
    crossinline onLanguageChange: (language: Language) -> Unit,
    crossinline onFeedbackButtonClick: () -> Unit,
    crossinline onHatButtonClick: () -> Unit,
    roomId: Value<String>,
    nickname: Value<String>,
    crossinline onRoomIdChange: (newRoomId: String) -> Unit,
    crossinline onNicknameChange: (newNickname: String) -> Unit,
): RealRoomEnterPageComponent =
    object : RealRoomEnterPageComponent(
        roomId = roomId,
        nickname = nickname,
    ) {
        override val theTrueHatPageWithHatComponent: TheTrueHatPageWithHatComponent =
            RealTheTrueHatPageWithHatComponent(
                backButtonEnabled = backButtonEnabled,
                onBackButtonClick = onBackButtonClick,
                onLanguageChange = onLanguageChange,
                onFeedbackButtonClick = onFeedbackButtonClick,
                onHatButtonClick = onHatButtonClick,
            )

        override fun onRoomIdChange(newRoomId: String) = onRoomIdChange(newRoomId)
        override fun onNicknameChange(newNickname: String) = onNicknameChange(newNickname)
    }

abstract class RealRoomEnterPageComponent(
    override val roomId: Value<String>,
    override val nickname: Value<String>,
): RoomEnterPageComponent