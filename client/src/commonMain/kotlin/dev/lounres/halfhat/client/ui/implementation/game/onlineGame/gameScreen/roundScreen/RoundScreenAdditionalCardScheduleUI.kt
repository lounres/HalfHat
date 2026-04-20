package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.RoundScreenComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameSpeakerToListenerRightArrow


@Composable
fun RoundScreenAdditionalCardScheduleUI(
    component: RoundScreenComponent,
    additionalCardChild: RoundScreenComponent.AdditionalCardChild.Schedule,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val gameState = component.gameState.collectAsState().value
        Text(
            text = "Current round",
            fontSize = 24.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Next round",
            fontSize = 24.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
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
                        text = gameState.playersList[gameState.nextSpeakerIndex].name,
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
                        text = gameState.playersList[gameState.nextListenerIndex].name,
                        autoSize = TextAutoSize.StepBased(maxFontSize = 32.sp),
                    )
                }
            }
        }
        when (val globalRole = gameState.selfRole.globalRole) {
            is ServerApi.OnlineGame.SelfRole.Round.GlobalRole.Player -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "You will be speaking in ${globalRole.roundsBeforeSpeaking} rounds",
                    fontSize = 20.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You will be listening in ${globalRole.roundsBeforeListening} rounds",
                    fontSize = 20.sp,
                )
            }
            is ServerApi.OnlineGame.SelfRole.GameInitialised.GlobalRole.Spectator -> {}
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${gameState.restWordsNumber + gameState.wordsInProgressNumber} (-${gameState.wordsInProgressNumber}) of ${gameState.initialWordsNumber} words left in the game",
            fontSize = 20.sp,
        )
    }
}