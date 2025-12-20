package dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundExplanation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.resources.Res
import dev.lounres.halfhat.client.resources.deviceGameListenerIcon_dark_png_24dp
import dev.lounres.halfhat.client.resources.deviceGameSpeakerIcon_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundExplanation.RoundExplanationComponent
import dev.lounres.halfhat.client.common.ui.utils.AutoScalingText
import org.jetbrains.compose.resources.painterResource


@Composable
public fun RowScope.RoundExplanationActionsUI(
    component: RoundExplanationComponent,
) {

}

@Composable
public fun ColumnScope.RoundExplanationUI(
    component: RoundExplanationComponent,
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
                        painter = painterResource(Res.drawable.deviceGameSpeakerIcon_dark_png_24dp),
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
                        painter = painterResource(Res.drawable.deviceGameListenerIcon_dark_png_24dp),
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
                    text = (gameState.millisecondsLeft / 1_000u + 1u).let { "${it / 60u}:${it % 60u}" },
                    fontSize = 32.sp,
                )
            }
        }
        when (val roundRole = gameState.role.roundRole) {
            ServerApi.OnlineGame.Role.RoundExplanation.RoundRole.Listener -> {}
            is ServerApi.OnlineGame.Role.RoundExplanation.RoundRole.Speaker -> {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    AutoScalingText(
                        text = roundRole.currentWord,
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
            ServerApi.OnlineGame.Role.RoundExplanation.RoundRole.Player -> {}
        }
    }
}

@Composable
public fun RowScope.RoundExplanationButtonsUI(
    component: RoundExplanationComponent,
) {

}