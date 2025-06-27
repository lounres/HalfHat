package dev.lounres.halfhat.client.common.ui.implementation.game.localGame

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.exitOnlineGameButton_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.game.localGame.LocalGamePageComponent
import dev.lounres.halfhat.client.common.ui.utils.WorkInProgress
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
public fun RowScope.LocalGamePageActionsUI(
    component: LocalGamePageComponent,
) {
    IconButton(
        onClick = component.onExitLocalGame
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(Res.drawable.exitOnlineGameButton_dark_png_24dp),
            contentDescription = "Exit device game"
        )
    }
}

@Composable
public fun LocalGamePageUI(
    component: LocalGamePageComponent,
) {
    WorkInProgress()
}