package dev.lounres.thetruehat.client.desktop.components.game.roundBreak

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language


interface RoundBreakPageComponent {
    val userRole: UserRole

    val backButtonEnabled: Boolean
    val wordsNumber: Value<Int>
    val volumeOn: Value<Boolean>
    val showFinishButton: Value<Boolean>
    val speakerNickname: Value<String>
    val listenerNickname: Value<String>
    val onBackButtonClick: () -> Unit
    val onLanguageChange: (language: Language) -> Unit
    val onFeedbackButtonClick: () -> Unit
    val onHatButtonClick: () -> Unit
    val onExitButtonClick: () -> Unit
    val onVolumeButtonClick: () -> Unit
    val onFinishButtonClick: () -> Unit


    sealed interface UserRole {
        data object SpeakerWaiting: UserRole
        data object ListenerWaiting: UserRole
        data object SpeakerReady: UserRole
        data object ListenerReady: UserRole
        data class SpeakerIn(val rounds: UInt): UserRole
        data class ListenerIn(val rounds: UInt): UserRole
    }
}