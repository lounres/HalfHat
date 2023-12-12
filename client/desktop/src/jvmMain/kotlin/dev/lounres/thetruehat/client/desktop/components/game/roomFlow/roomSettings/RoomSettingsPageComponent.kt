package dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomSettings

import dev.lounres.thetruehat.api.localization.Language


interface RoomSettingsPageComponent {
    val backButtonEnabled: Boolean
    val onBackButtonClick: () -> Unit
    val onLanguageChange: (language: Language) -> Unit
    val onFeedbackButtonClick: () -> Unit
    val onHatButtonClick: () -> Unit
}