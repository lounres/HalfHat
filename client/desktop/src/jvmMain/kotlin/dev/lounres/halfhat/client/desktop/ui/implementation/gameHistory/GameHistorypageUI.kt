package dev.lounres.halfhat.client.desktop.ui.implementation.gameHistory

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.desktop.resources.gameHistoryPage_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.gameHistory.GameHistoryPageComponent
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
fun GameHistoryPageIcon(
    isSelected: Boolean,
) {
    Icon(
        painter = painterResource(DesktopRes.drawable.gameHistoryPage_dark_png_24dp),
        modifier = commonIconModifier,
        contentDescription = "Game history page",
    )
}

@Composable
fun GameHistoryPageBadge(
    component: GameHistoryPageComponent,
    isSelected: Boolean,
) {

}