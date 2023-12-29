package dev.lounres.thetruehat.client.common.components.home

import dev.lounres.thetruehat.api.localization.Language


public interface HomePageComponent {
    public val backButtonEnabled: Boolean
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (language: Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit

    public val onCreateOnlineGameButtonClick: () -> Unit
    public val onEnterOnlineGameButtonClick: () -> Unit

    public val onCreateGameTimerButtonClick: () -> Unit
}