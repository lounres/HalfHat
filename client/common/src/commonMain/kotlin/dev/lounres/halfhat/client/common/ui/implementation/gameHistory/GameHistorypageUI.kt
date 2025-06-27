package dev.lounres.halfhat.client.common.ui.implementation.gameHistory

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.gameHistoryPage_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.gameHistory.GameHistoryPageComponent
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
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