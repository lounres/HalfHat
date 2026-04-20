package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundPreparation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundPreparation.RoundPreparationComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameSpeakerToListenerRightArrow
import kotlin.math.min


@Composable
fun RoundPreparationGameCardUI(
    component: RoundPreparationComponent,
) {
    val gameState = component.gameState.collectAsState().value
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (val globalRole = gameState.selfRole.globalRole) {
            is ServerApi.OnlineGame.SelfRole.Round.Preparation.GlobalRole.Player ->
                when (val roundRole = globalRole.roundRole) {
                    ServerApi.OnlineGame.SelfRole.Round.Preparation.GlobalRole.Player.RoundRole.Speaker ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "You explain",
                                fontSize = 48.sp,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${gameState.playersList[gameState.listenerIndex].name} guesses"
                            )
                        }
                    ServerApi.OnlineGame.SelfRole.Round.Preparation.GlobalRole.Player.RoundRole.Listener ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "You guess",
                                fontSize = 48.sp,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${gameState.playersList[gameState.speakerIndex].name} explains"
                            )
                        }
                    null ->
                        Row(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                        ) {
                            Card(
                                modifier = Modifier.fillMaxHeight().weight(2f),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                ),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = gameState.playersList[gameState.speakerIndex].name,
                                        autoSize = TextAutoSize.StepBased(maxFontSize = 32.sp),
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier.fillMaxHeight().weight(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    modifier = Modifier.fillMaxSize(1f / 2),
                                    imageVector = HalfHatIcon.OnlineGameSpeakerToListenerRightArrow,
                                    contentDescription = null,
                                )
                            }
                            Card(
                                modifier = Modifier.fillMaxHeight().weight(2f),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                ),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = gameState.playersList[gameState.listenerIndex].name,
                                        autoSize = TextAutoSize.StepBased(maxFontSize = 32.sp),
                                    )
                                }
                            }
                        }
                }
            is ServerApi.OnlineGame.SelfRole.Round.Preparation.GlobalRole.Spectator -> {}
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val millisecondsLeft = gameState.millisecondsLeft
            Text(
                text = ((millisecondsLeft + 999u) / 1_000u).toString(),
                fontSize = 256.sp,
                color = Color.hsv(
                    hue = min(millisecondsLeft, 3_000u).toInt() * 0.04f,
                    saturation = 1f,
                    value = 1f,
                ),
            )
        }
        when (val globalRole = gameState.selfRole.globalRole) {
            is ServerApi.OnlineGame.SelfRole.Round.Preparation.GlobalRole.Player ->
                when (val roundRole = globalRole.roundRole) {
                    ServerApi.OnlineGame.SelfRole.Round.Preparation.GlobalRole.Player.RoundRole.Speaker ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
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
                    ServerApi.OnlineGame.SelfRole.Round.Preparation.GlobalRole.Player.RoundRole.Listener -> {}
                    null -> {}
                }
            is ServerApi.OnlineGame.SelfRole.Round.Preparation.GlobalRole.Spectator -> {}
        }
    }
}