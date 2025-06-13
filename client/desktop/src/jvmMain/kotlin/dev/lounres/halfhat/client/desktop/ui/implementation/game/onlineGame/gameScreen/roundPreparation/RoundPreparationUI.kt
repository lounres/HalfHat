package dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roundPreparation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import dev.lounres.halfhat.client.desktop.resources.deviceGameListenerIcon_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.deviceGameSpeakerIcon_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.exitDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.onlineGameKey_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.onlineGameLink_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundPreparation.RoundPreparationComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import org.jetbrains.compose.resources.painterResource
import kotlin.math.min


@Composable
fun RowScope.RoundPreparationActionsUI(
    component: RoundPreparationComponent,
) {

}

@Composable
fun ColumnScope.RoundPreparationUI(
    component: RoundPreparationComponent,
) {
    val gameState by component.gameState.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(DesktopRes.drawable.deviceGameSpeakerIcon_dark_png_24dp),
                        modifier = Modifier.size(24.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${gameState.playersList[gameState.speakerIndex]} explains",
                        fontSize = 16.sp,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${gameState.playersList[gameState.listenerIndex]} guesses",
                        fontSize = 16.sp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(DesktopRes.drawable.deviceGameListenerIcon_dark_png_24dp),
                        modifier = Modifier.size(24.dp),
                        contentDescription = null
                    )
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val millisecondsLeft = gameState.millisecondsLeft
            Text(
                text = (millisecondsLeft / 1_000u + 1u).toString(),
                fontSize = 256.sp,
                color = Color.hsv(
                    hue = min(millisecondsLeft, 3_000u).toInt() * 0.04f,
                    saturation = 1f,
                    value = 1f,
                ),
            )
        }
        // TODO: Are the buttons needed (being disabled)
        if (gameState.role.userIndex == gameState.speakerIndex)
            Column(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        enabled = false,
                        onClick = {},
                    ) {
                        Text(
                            text = "Not guessed",
                            fontSize = 16.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        enabled = false,
                        onClick = {},
                    ) {
                        Text(
                            text = "Mistake",
                            fontSize = 16.sp,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    enabled = false,
                    onClick = {},
                ) {
                    Text(
                        text = "Guessed",
                        fontSize = 32.sp,
                    )
                }
            }
    }
}

@Composable
fun RowScope.RoundPreparationButtonsUI(
    component: RoundPreparationComponent,
) {

}