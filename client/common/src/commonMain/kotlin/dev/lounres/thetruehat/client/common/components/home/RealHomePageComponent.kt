package dev.lounres.thetruehat.client.common.components.home

import dev.lounres.thetruehat.api.localization.Language


public class RealHomePageComponent(
    override val backButtonEnabled: Boolean,
    override val onBackButtonClick: () -> Unit,
    override val onLanguageChange: (language: Language) -> Unit,
    override val onFeedbackButtonClick: () -> Unit,
    override val onHatButtonClick: () -> Unit,

    override val onCreateOnlineGameButtonClick: () -> Unit,
    override val onEnterOnlineGameButtonClick: () -> Unit,

    override val onCreateGameTimerButtonClick: () -> Unit
) : HomePageComponent