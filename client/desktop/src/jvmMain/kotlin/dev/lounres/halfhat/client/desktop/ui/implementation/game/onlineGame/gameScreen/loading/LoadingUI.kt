package dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.desktop.resources.exitDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.gameSettingsaButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.onlineGameKey_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.onlineGameLink_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.startDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.loading.LoadingComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import org.jetbrains.compose.resources.painterResource


@Composable
fun RowScope.LoadingActionsUI(
    component: LoadingComponent
) {

}

@Composable
fun ColumnScope.LoadingUI(
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
            CircularProgressIndicator()
        }
    }
}

@Composable
fun RowScope.LoadingButtonsUI(
    component: LoadingComponent
) {

}