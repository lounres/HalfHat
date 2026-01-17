package dev.lounres.halfhat.client.ui.implementation.miscellanea.gameHistory

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.ui.components.miscellanea.gameHistory.GameHistoryPageComponent
import dev.lounres.halfhat.client.ui.icons.GameHistoryIcon
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.utils.commonIconModifier


@Composable
public fun GameHistoryPageIcon(
    isSelected: Boolean,
) {
    Icon(
        imageVector = HalfHatIcon.GameHistoryIcon,
        modifier = commonIconModifier,
        contentDescription = "Game history page",
    )
}

@Composable
public fun GameHistoryPageBadge(
    component: dev.lounres.halfhat.client.ui.components.miscellanea.gameHistory.GameHistoryPageComponent,
    isSelected: Boolean,
) {

}