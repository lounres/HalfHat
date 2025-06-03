package dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.roundPreparation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import dev.lounres.halfhat.client.desktop.resources.deviceGameListenerIcon_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.deviceGameSpeakerIcon_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.exitDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundPreparation.RoundPreparationComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import org.jetbrains.compose.resources.painterResource
import kotlin.math.min


@Composable
fun RowScope.RoundPreparationActionsUI(
    component: RoundPreparationComponent,
) {
    IconButton(
        onClick = component.onExitGame
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.exitDeviceGameButton_dark_png_24dp),
            contentDescription = "Exit device game"
        )
    }
}

@Composable
fun RoundPreparationUI(
    component: RoundPreparationComponent,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().widthIn(max = 480.dp).align(Alignment.Center).padding(16.dp),
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
                            text = "${component.speaker.collectAsState().value} explains",
                            fontSize = 16.sp,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${component.listener.collectAsState().value} guesses",
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
                val millisecondsLeft = component.millisecondsLeft.collectAsState().value
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
//            Column(
//                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
//                horizontalAlignment = Alignment.CenterHorizontally,
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                ) {
//                    Button(
//                        modifier = Modifier.weight(1f),
//                        shape = CircleShape,
//                        onClick = onNotGuessed,
//                    ) {
//                        Text(
//                            text = "Not guessed",
//                            fontSize = 16.sp,
//                        )
//                    }
//                    Spacer(modifier = Modifier.width(16.dp))
//                    Button(
//                        modifier = Modifier.weight(1f),
//                        shape = CircleShape,
//                        onClick = onMistake,
//                    ) {
//                        Text(
//                            text = "Mistake",
//                            fontSize = 16.sp,
//                        )
//                    }
//                }
//                Spacer(modifier = Modifier.height(8.dp))
//                Button(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = CircleShape,
//                    onClick = onGuessed,
//                ) {
//                    Text(
//                        text = "Guessed",
//                        fontSize = 32.sp,
//                    )
//                }
//            }
        }
    }
}