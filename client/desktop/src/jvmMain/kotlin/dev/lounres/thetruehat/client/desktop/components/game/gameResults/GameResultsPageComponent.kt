package dev.lounres.thetruehat.client.desktop.components.game.gameResults

import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription


interface GameResultsPageComponent {
    val backButtonEnabled: Boolean
    val onBackButtonClick: () -> Unit
    val onLanguageChange: (Language) -> Unit
    val onFeedbackButtonClick: () -> Unit
    val onHatButtonClick: () -> Unit

    val resultList: List<RoomDescription.GameResult>
}