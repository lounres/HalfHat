package dev.lounres.thetruehat.client.common.components.game.roundBreak

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language


public interface RoundBreakPageComponent {
    public val userRole: UserRole

    public val backButtonEnabled: Boolean
    public val wordsNumber: Value<Int>
    public val volumeOn: Value<Boolean>
    public val showFinishButton: Value<Boolean>
    public val speakerNickname: Value<String>
    public val listenerNickname: Value<String>
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (language: Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit
    public val onExitButtonClick: () -> Unit
    public val onVolumeButtonClick: () -> Unit
    public val onFinishButtonClick: () -> Unit


    public sealed interface UserRole {
        public data object SpeakerWaiting: UserRole
        public data object ListenerWaiting: UserRole
        public data object SpeakerReady: UserRole
        public data object ListenerReady: UserRole
        public data class SpeakerIn(val rounds: UInt): UserRole
        public data class ListenerIn(val rounds: UInt): UserRole
    }
}