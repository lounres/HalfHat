package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundLastGuess

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundLastGuess.RoundLastGuessComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameSpeakerToListenerRightArrow


@Composable
public fun RoundLastGuessGameCardUI(
    component: RoundLastGuessComponent,
) {
    val gameState by component.gameState.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (gameState.role.roundRole) {
            is ServerApi.OnlineGame.Role.Round.LastGuess.RoundRole.Speaker ->
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
            ServerApi.OnlineGame.Role.Round.LastGuess.RoundRole.Listener ->
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
            ServerApi.OnlineGame.Role.Round.LastGuess.RoundRole.Player ->
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
                text = (gameState.millisecondsLeft / 100u).let { "${it / 10u}.${it % 10u}" },
                fontSize = 64.sp,
                color = Color.Red
            )
        }
        when (val roundRole = gameState.role.roundRole) {
            is ServerApi.OnlineGame.Role.Round.LastGuess.RoundRole.Speaker -> {
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
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            onClick = component.onNotGuessed,
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
                    ) {
                        Text(
                            text = "Guessed",
                            fontSize = 32.sp,
                        )
                    }
                }
            }
            ServerApi.OnlineGame.Role.Round.LastGuess.RoundRole.Listener -> {}
            ServerApi.OnlineGame.Role.Round.LastGuess.RoundRole.Player -> {}
        }
    }
}