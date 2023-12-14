package dev.lounres.thetruehat.client.common.components.home

import dev.lounres.thetruehat.api.localization.Language


public interface HomePageComponent {
    public val backButtonEnabled: Boolean
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (language: Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit
    public val onCreateButtonClick: () -> Unit
    public val onEnterButtonClick: () -> Unit
}