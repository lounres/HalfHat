package dev.lounres.halfhat.client.ui.implementation.game.deviceGame.gameScreen.roundWaiting

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
import dev.lounres.halfhat.client.ui.components.game.deviceGame.gameScreen.roundWaiting.RoundWaitingComponent
import dev.lounres.halfhat.client.ui.icons.DeviceGameExitModeButton
import dev.lounres.halfhat.client.ui.icons.DeviceGameFinishGameButton
import dev.lounres.halfhat.client.ui.icons.DeviceGameListenerIcon
import dev.lounres.halfhat.client.ui.icons.DeviceGameSpeakerIcon
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
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
            imageVector = HalfHatIcon.DeviceGameFinishGameButton,
            contentDescription = "Finish device game"
        )
    }
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
                        imageVector = HalfHatIcon.DeviceGameSpeakerIcon,
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
                        imageVector = HalfHatIcon.DeviceGameListenerIcon,
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