package dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomSettings

import dev.lounres.thetruehat.api.localization.Language


class FakeRoomSettingsPageComponent(
    override val backButtonEnabled: Boolean = true,
) : RoomSettingsPageComponent {
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (language: Language) -> Unit = {}
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}
}