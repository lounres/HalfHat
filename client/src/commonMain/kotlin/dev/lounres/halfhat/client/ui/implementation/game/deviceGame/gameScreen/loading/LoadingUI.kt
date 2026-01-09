package dev.lounres.halfhat.client.ui.implementation.game.deviceGame.gameScreen.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.ui.components.game.deviceGame.gameScreen.loading.LoadingComponent
import dev.lounres.halfhat.client.ui.icons.DeviceGameExitModeButton
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.utils.commonIconModifier


@Composable
public fun RowScope.LoadingActionsUI(
    component: LoadingComponent
) {
    IconButton(
        onClick = component.onExitGame
    ) {
        Icon(
            modifier = commonIconModifier,
            imageVector = HalfHatIcon.DeviceGameExitModeButton,
            contentDescription = "Exit device game"
        )
    }
}

@Composable
public fun LoadingUI(
    component: LoadingComponent
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Please, wait.",
                fontSize = 32.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Game is loading...",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            ContainedLoadingIndicator()
        }
    }
}