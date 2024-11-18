package dev.lounres.halfhat.client.desktop.ui.implementation.game.localGame

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.desktop.resources.exitOnlineGameButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.game.localGame.LocalGamePageComponent
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import dev.lounres.halfhat.client.desktop.ui.utils.WorkInProgress
import org.jetbrains.compose.resources.painterResource


@Composable
fun RowScope.LocalGamePageActionsUI(
    component: LocalGamePageComponent,
) {
    IconButton(
        onClick = component.onExitLocalGame
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.exitOnlineGameButton_dark_png_24dp),
            contentDescription = "Exit device game"
        )
    }
}

@Composable
fun LocalGamePageUI(
    component: LocalGamePageComponent,
) {
    WorkInProgress()
}