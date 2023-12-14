package dev.lounres.thetruehat.client.common.components.game.gameResults

import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription


public class FakeGameResultsPageComponent(
    override val backButtonEnabled: Boolean = true,
    override val resultList: List<RoomDescription.GameResult> = listOf(
        RoomDescription.GameResult("Panther", 1, 2),
        RoomDescription.GameResult("Jaguar", 3, 4),
        RoomDescription.GameResult("Tiger", 5, 6),
        RoomDescription.GameResult("Lion", 7, 8),
    ),
): GameResultsPageComponent {
    override val onBackButtonClick: () -> Unit = {}
    override val onLanguageChange: (Language) -> Unit = {}
    override val onFeedbackButtonClick: () -> Unit = {}
    override val onHatButtonClick: () -> Unit = {}
}