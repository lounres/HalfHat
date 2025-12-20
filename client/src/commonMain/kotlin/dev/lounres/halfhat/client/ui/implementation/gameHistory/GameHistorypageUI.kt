package dev.lounres.halfhat.client.ui.implementation.gameHistory

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.resources.Res
import dev.lounres.halfhat.client.resources.gameHistoryPage_dark_png_24dp
import dev.lounres.halfhat.client.ui.components.gameHistory.GameHistoryPageComponent
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
public fun GameHistoryPageIcon(
    isSelected: Boolean,
) {
    Icon(
        painter = painterResource(Res.drawable.gameHistoryPage_dark_png_24dp),
        modifier = commonIconModifier,
        contentDescription = "Game history page",
    )
}

@Composable
public fun GameHistoryPageBadge(
    component: GameHistoryPageComponent,
    isSelected: Boolean,
) {

}