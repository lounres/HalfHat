package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundExplanation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundExplanation.RoundExplanationComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameSpeakerToListenerRightArrow
import dev.lounres.kone.hub.subscribeAsState


@Composable
fun RoundExplanationGameCardUI(
    component: RoundExplanationComponent,
) {
    val gameState = component.gameState.collectAsState().value
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (val globalRole = gameState.selfRole.globalRole) {
            is ServerApi.OnlineGame.SelfRole.Round.Explanation.GlobalRole.Player ->
                when (globalRole.roundRole) {
                    is ServerApi.OnlineGame.SelfRole.Round.Explanation.GlobalRole.Player.RoundRole.Speaker ->
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

                    ServerApi.OnlineGame.SelfRole.Round.Explanation.GlobalRole.Player.RoundRole.Listener ->
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
            is ServerApi.OnlineGame.SelfRole.Round.Explanation.GlobalRole.Spectator ->
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
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = (gameState.millisecondsLeft / 1_000u).let { "${it / 60u}:${(it % 60u).toString().padStart(2, '0')}" },
                fontSize = 32.sp,
            )
        }
        when (val globalRole = gameState.selfRole.globalRole) {
            is ServerApi.OnlineGame.SelfRole.Round.Explanation.GlobalRole.Player ->
                when (val roundRole = globalRole.roundRole) {
                    is ServerApi.OnlineGame.SelfRole.Round.Explanation.GlobalRole.Player.RoundRole.Speaker -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = roundRole.currentWord,
                                autoSize = TextAutoSize.StepBased(),
                                softWrap = false,
                                maxLines = 1,
                            )
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val useDark = component.darkTheme.subscribeAsState().value.isDark
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    onClick = component.onNotGuessed,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor =
                                            if (useDark) Color(0xFFAAC7FF)
                                            else Color(0xFF415F91),
                                        contentColor =
                                            if (useDark) Color(0xFF0A305F)
                                            else Color(0xFFFFFFFF),
                                    ),
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
                                    onClick = component.onMistake,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor =
                                            if (useDark) Color(0xFFFFB5A0)
                                            else Color(0xFF8F4C38),
                                        contentColor =
                                            if (useDark) Color(0xFF561F0F)
                                            else Color(0xFFFFFFFF),
                                    ),
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
                                onClick = component.onGuessed,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor =
                                        if (useDark) Color(0xFFB1D18A)
                                        else Color(0xFF4C662B),
                                    contentColor =
                                        if (useDark) Color(0xFF1F3701)
                                        else Color(0xFFFFFFFF),
                                ),
                            ) {
                                Text(
                                    text = "Guessed",
                                    fontSize = 32.sp,
                                )
                            }
                        }
                    }
                    ServerApi.OnlineGame.SelfRole.Round.Explanation.GlobalRole.Player.RoundRole.Listener -> {}
                    null -> {}
                }
            is ServerApi.OnlineGame.SelfRole.Round.Explanation.GlobalRole.Spectator -> {}
        }
    }
}