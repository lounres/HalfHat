package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.loading

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.loading.LoadingComponent


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
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = component.onExitOnlineGame,
            ) {
                Text(
                    text = "Leave",
                    fontSize = 24.sp,
                )
            }
        }
    }
}