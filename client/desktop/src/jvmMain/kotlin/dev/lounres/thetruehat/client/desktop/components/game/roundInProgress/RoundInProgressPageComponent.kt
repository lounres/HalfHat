package dev.lounres.thetruehat.client.desktop.components.game.roundInProgress

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language


interface RoundInProgressPageComponent {
    val backButtonEnabled: Boolean
    val wordsNumber: Int
    val showFinishButton: Boolean
    val volumeOn: Boolean
    val speakerNickname: String
    val listenerNickname: String
    val userRole: RoundInProgressUserRole
    val timeLeft: Value<Int>
    val onBackButtonClick: () -> Unit
    val onLanguageChange: (language: Language) -> Unit
    val onFeedbackButtonClick: () -> Unit
    val onHatButtonClick: () -> Unit
    val onVolumeButtonClick: () -> Unit
    val onFinishButtonClick: () -> Unit
    val onExitButtonClick: () -> Unit


    sealed interface RoundInProgressUserRole {
        data class Speaker(val wordToExplain: Value<String>): RoundInProgressUserRole
        data object Listener: RoundInProgressUserRole
        data class SpeakerIn(val rounds: UInt): RoundInProgressUserRole
        data class ListenerIn(val rounds: UInt): RoundInProgressUserRole
    }
}