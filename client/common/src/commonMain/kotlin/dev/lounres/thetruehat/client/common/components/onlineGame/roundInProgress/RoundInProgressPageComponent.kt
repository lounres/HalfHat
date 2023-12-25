package dev.lounres.thetruehat.client.common.components.onlineGame.roundInProgress

import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState


public interface RoundInProgressPageComponent {
    public val backButtonEnabled: Boolean
    public val unitsUntilEnd: Value<UserGameState.UnitsUntilEnd>
    public val showFinishButton: Value<Boolean>
    public val volumeOn: Value<Boolean>
    public val speakerNickname: Value<String>
    public val listenerNickname: Value<String>
    public val userRole: Value<UserRole>
    public val countsUntilEnd: Value<Long>
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (language: Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit
    public val onVolumeButtonClick: () -> Unit
    public val onFinishButtonClick: () -> Unit
    public val onExitButtonClick: () -> Unit

    public val onExplainedButtonClick: () -> Unit
    public val onNotExplainedButtonClick: () -> Unit
    public val onImproperlyExplainedButtonClick: () -> Unit


    public sealed interface UserRole {
        public data class Speaker(public val wordToExplain: String): UserRole
        public data object Listener: UserRole
        public data class SpeakerIn(public val rounds: UInt): UserRole
        public data class ListenerIn(public val rounds: UInt): UserRole
    }
}