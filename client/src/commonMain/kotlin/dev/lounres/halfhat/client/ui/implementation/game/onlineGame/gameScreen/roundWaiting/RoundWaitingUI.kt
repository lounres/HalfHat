package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundWaiting

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundWaiting.RoundWaitingComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameFinishGameButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameListenerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameSpeakerIcon
import dev.lounres.halfhat.client.ui.utils.commonIconModifier


@Composable
public fun RowScope.RoundWaitingActionsUI(
    component: RoundWaitingComponent,
) {
//    IconButton(
//        onClick = component.onFinishGame
//    ) {
//        Icon(
//            modifier = commonIconModifier,
//            painter = painterResource(Res.drawable.finishDeviceGameButton_dark_png_24dp), // TODO: Copy the icons
//            contentDescription = "Finish online game"
//        )
//    }
}

@Composable
public fun ColumnScope.RoundWaitingUI(
    component: RoundWaitingComponent,
) {
    val gameState by component.gameState.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
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
                    imageVector = HalfHatIcon.OnlineGameSpeakerIcon,
                    modifier = Modifier.size(24.dp),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${gameState.playersList[gameState.speakerIndex].name} explains",
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
                    text = "${gameState.playersList[gameState.listenerIndex].name} guesses",
                    fontSize = 24.sp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = HalfHatIcon.OnlineGameListenerIcon,
                    modifier = Modifier.size(24.dp),
                    contentDescription = null
                )
            }
        }
        when {
            gameState.role.userIndex == gameState.speakerIndex -> {
                Spacer(modifier = Modifier.height(8.dp))
                if (!gameState.speakerReady)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        onClick = component.onSpeakerReady,
                    ) {
                        Text(
                            text = "I am ready to explain",
                            fontSize = 32.sp,
                        )
                    }
                else
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        shape = CircleShape,
                        onClick = component.onSpeakerReady,
                    ) {
                        Text(
                            text = "Wait for your partner",
                            fontSize = 32.sp,
                        )
                    }
            }
            gameState.role.userIndex == gameState.listenerIndex -> {
                Spacer(modifier = Modifier.height(8.dp))
                if (!gameState.listenerReady)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        onClick = component.onListenerReady,
                    ) {
                        Text(
                            text = "I am ready to guess",
                            fontSize = 32.sp,
                        )
                    }
                else
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        shape = CircleShape,
                        onClick = component.onListenerReady,
                    ) {
                        Text(
                            text = "Wait for your partner",
                            fontSize = 32.sp,
                        )
                    }
            }
            else -> {}
        }
    }
}

@Composable
public fun RowScope.RoundWaitingButtonsUI(
    component: RoundWaitingComponent,
) {
    if (component.gameState.collectAsState().value.role.isHost)
        IconButton(
            onClick = component.onFinishGame
        ) {
            Icon(
                modifier = commonIconModifier,
                imageVector = HalfHatIcon.OnlineGameFinishGameButton,
                contentDescription = "Finish online game"
            )
        }
}