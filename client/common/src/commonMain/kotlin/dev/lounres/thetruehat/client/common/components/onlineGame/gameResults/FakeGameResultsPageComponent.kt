package dev.lounres.thetruehat.client.common.components.onlineGame.gameResults

import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState


public class FakeGameResultsPageComponent(
    override val backButtonEnabled: Boolean = true,
    override val resultList: List<UserGameState.GameResult> = listOf(
        UserGameState.GameResult("Panther", 1, 2),
        UserGameState.GameResult("Jaguar", 3, 4),
        UserGameState.GameResult("Tiger", 5, 6),
        UserGameState.GameResult("Lion", 7, 8),
    ),
): GameResultsPageComponent {
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (Language) -> Unit = {}
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}
}