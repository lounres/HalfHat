package dev.lounres.halfhat.client.ui.implementation.game.localGame

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.ui.components.game.localGame.LocalGamePageComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.LocalGameExitModeButton
import dev.lounres.halfhat.client.ui.utils.WorkInProgress
import dev.lounres.halfhat.client.ui.utils.commonIconModifier


@Composable
public fun RowScope.LocalGamePageActionsUI(
    component: LocalGamePageComponent,
) {
    IconButton(
        onClick = component.onExitLocalGame
    ) {
        Icon(
            modifier = commonIconModifier,
            imageVector = HalfHatIcon.LocalGameExitModeButton,
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