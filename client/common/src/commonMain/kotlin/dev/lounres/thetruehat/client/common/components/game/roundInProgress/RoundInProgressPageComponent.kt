package dev.lounres.thetruehat.client.common.components.game.roundInProgress

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language


public interface RoundInProgressPageComponent {
    public val backButtonEnabled: Boolean
    public val wordsNumber: Int
    public val showFinishButton: Boolean
    public val volumeOn: Boolean
    public val speakerNickname: String
    public val listenerNickname: String
    public val userRole: RoundInProgressUserRole
    public val timeLeft: Value<Int>
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (language: Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit
    public val onVolumeButtonClick: () -> Unit
    public val onFinishButtonClick: () -> Unit
    public val onExitButtonClick: () -> Unit


    public sealed interface RoundInProgressUserRole {
        public data class Speaker(public val wordToExplain: Value<String>): RoundInProgressUserRole
        public data object Listener: RoundInProgressUserRole
        public data class SpeakerIn(public val rounds: UInt): RoundInProgressUserRole
        public data class ListenerIn(public val rounds: UInt): RoundInProgressUserRole
    }
}