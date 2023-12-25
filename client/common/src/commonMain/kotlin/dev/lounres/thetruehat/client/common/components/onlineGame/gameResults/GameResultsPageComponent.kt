package dev.lounres.thetruehat.client.common.components.onlineGame.gameResults

import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState


public interface GameResultsPageComponent {
    public val backButtonEnabled: Boolean
    public val onBackButtonClick: () -> Unit
    public val onLanguageChange: (Language) -> Unit
    public val onFeedbackButtonClick: () -> Unit
    public val onHatButtonClick: () -> Unit

    public val resultList: List<UserGameState.GameResult>
}