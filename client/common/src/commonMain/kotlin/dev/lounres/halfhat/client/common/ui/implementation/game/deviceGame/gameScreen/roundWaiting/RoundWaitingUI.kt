package dev.lounres.halfhat.client.common.ui.implementation.game.deviceGame.gameScreen.roundWaiting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.deviceGameListenerIcon_dark_png_24dp
import dev.lounres.halfhat.client.common.resources.deviceGameSpeakerIcon_dark_png_24dp
import dev.lounres.halfhat.client.common.resources.exitDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.common.resources.finishDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundWaiting.RoundWaitingComponent
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
public fun RowScope.RoundWaitingActionsUI(
    component: RoundWaitingComponent,
) {
    IconButton(
        onClick = component.onFinishGame
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(Res.drawable.finishDeviceGameButton_dark_png_24dp),
            contentDescription = "Finish device game"
        )
    }
    IconButton(
        onClick = component.onExitGame
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(Res.drawable.exitDeviceGameButton_dark_png_24dp),
            contentDescription = "Exit device game"
        )
    }
}

@Composable
public fun RoundWaitingUI(
    component: RoundWaitingComponent,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().widthIn(max = 480.dp).align(Alignment.Center).padding(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.deviceGameSpeakerIcon_dark_png_24dp),
                        modifier = Modifier.size(24.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${component.speaker.collectAsState().value} explains",
                        fontSize = 24.sp,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "${component.listener.collectAsState().value} guesses",
                        fontSize = 24.sp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(Res.drawable.deviceGameListenerIcon_dark_png_24dp),
                        modifier = Modifier.size(24.dp),
                        contentDescription = null
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                onClick = component.onStartRound,
            ) {
                Text(
                    text = "Start",
                    fontSize = 32.sp,
                )
            }
        }
    }
}