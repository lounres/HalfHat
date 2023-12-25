package dev.lounres.thetruehat.client.common.components.onlineGame.gameResults

import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState


public class RealGameResultsPageComponent(
    override val backButtonEnabled: Boolean,
    override val onBackButtonClick: () -> Unit,
    override val onLanguageChange: (Language) -> Unit,
    override val onFeedbackButtonClick: () -> Unit,
    override val onHatButtonClick: () -> Unit,

    override val resultList: List<UserGameState.GameResult>
) : GameResultsPageComponent