package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundLastGuess

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameListenerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameSpeakerIcon


@Composable
public fun RowScope.RoundLastGuessActionsUI(
    component: RoundLastGuessComponent,
) {

}

@Composable
public fun ColumnScope.RoundLastGuessUI(
    component: RoundLastGuessComponent,
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
                        imageVector = HalfHatIcon.OnlineGameSpeakerIcon,
                        modifier = Modifier.size(24.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${gameState.playersList[gameState.speakerIndex].name} explains",
                        fontSize = 16.sp,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${gameState.playersList[gameState.listenerIndex].name} guesses",
                        fontSize = 16.sp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = HalfHatIcon.OnlineGameListenerIcon,
                        modifier = Modifier.size(24.dp),
                        contentDescription = null
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = (gameState.millisecondsLeft / 100u + 1u).let { "${it / 10u}.${it % 10u}" },
                    fontSize = 64.sp,
                    color = Color.Red
                )
            }
        }
        when (val roundRole = gameState.role.roundRole) {
            ServerApi.OnlineGame.Role.RoundLastGuess.RoundRole.Listener -> {}
            is ServerApi.OnlineGame.Role.RoundLastGuess.RoundRole.Speaker -> {
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
            ServerApi.OnlineGame.Role.RoundLastGuess.RoundRole.Player -> {}
        }
    }
}

@Composable
public fun RowScope.RoundLastGuessButtonsUI(
    component: RoundLastGuessComponent,
) {

}