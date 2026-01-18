package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundWaiting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundWaiting.RoundWaitingComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameListenerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameSpeakerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameSpeakerToListenerDownArrow


@Composable
fun RoundWaitingGameCardUI(
    component: RoundWaitingComponent,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val gameState = component.gameState.collectAsState().value
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors =
                    if (gameState.speakerReady)
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                        )
                    else
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        ),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            modifier = Modifier.size(60.dp),
                            imageVector = HalfHatIcon.OnlineGameSpeakerIcon,
                            contentDescription = null
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = gameState.playersList[gameState.speakerIndex].name,
                            fontSize = 48.sp,
                        )
                        Text(
                            text = "explains",
                            fontSize = 24.sp,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.fillMaxSize(1f / 2),
                    imageVector = HalfHatIcon.OnlineGameSpeakerToListenerDownArrow,
                    contentDescription = null,
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors =
                    if (gameState.listenerReady)
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                        )
                    else
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        ),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = gameState.playersList[gameState.listenerIndex].name,
                            fontSize = 48.sp,
                        )
                        Text(
                            text = "guesses",
                            fontSize = 24.sp,
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            modifier = Modifier.size(60.dp),
                            imageVector = HalfHatIcon.OnlineGameListenerIcon,
                            contentDescription = null
                        )
                    }
                }
            }
        }
        when (gameState.role.roundRole) {
            ServerApi.OnlineGame.Role.Round.Waiting.RoundRole.Speaker -> {
                Spacer(modifier = Modifier.height(32.dp))
                if (!gameState.speakerReady)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        onClick = component.onSpeakerReady,
                    ) {
                        Text(
                            text = "I am ready",
                            maxLines = 1,
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
                            maxLines = 1,
                            fontSize = 32.sp,
                        )
                    }
            }
            ServerApi.OnlineGame.Role.Round.Waiting.RoundRole.Listener -> {
                Spacer(modifier = Modifier.height(32.dp))
                if (!gameState.listenerReady)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        onClick = component.onListenerReady,
                    ) {
                        Text(
                            text = "I am ready",
                            maxLines = 1,
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
                            maxLines = 1,
                            fontSize = 32.sp,
                        )
                    }
            }
            ServerApi.OnlineGame.Role.Round.Waiting.RoundRole.Player -> {}
        }
    }
}