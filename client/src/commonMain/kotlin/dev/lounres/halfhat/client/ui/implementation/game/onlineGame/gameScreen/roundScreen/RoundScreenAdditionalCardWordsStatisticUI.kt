package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.RoundScreenComponent
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.hub.subscribeAsState


@Composable
fun RoundScreenAdditionalCardWordsStatisticUI(
    component: RoundScreenComponent,
    additionalCardChild: RoundScreenComponent.AdditionalCardChild.WordsStatistic,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val gameState = component.gameState.collectAsState().value
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp).height(IntrinsicSize.Min),
        ) {
//                            Spacer(modifier = Modifier.width(40.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = "Word",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp),
                maxLines = 1,
                softWrap = false,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "Time spent",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp),
                maxLines = 1,
                softWrap = false,
            )
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
        )
        val useDark = component.darkTheme.subscribeAsState().value.isDark
        for ((word, spentTime, state) in additionalCardChild.wordsStatistic) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = CircleShape,
                color = when (state) {
                    GameStateMachine.WordStatistic.State.Explained -> if (useDark) Color(0xFFB1D18A) else Color(0xFF4C662B)
                    GameStateMachine.WordStatistic.State.InProgress -> if (useDark) Color(0xFFAAC7FF) else Color(0xFF415F91)
                    GameStateMachine.WordStatistic.State.Mistake -> if (useDark) Color(0xFFFFB5A0) else Color(0xFF8F4C38)
                },
                contentColor = when (state) {
                    GameStateMachine.WordStatistic.State.Explained -> if (useDark) Color(0xFF1F3701) else Color(0xFFFFFFFF)
                    GameStateMachine.WordStatistic.State.InProgress -> if (useDark) Color(0xFF0A305F) else Color(0xFFFFFFFF)
                    GameStateMachine.WordStatistic.State.Mistake -> if (useDark) Color(0xFF561F0F) else Color(0xFFFFFFFF)
                },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
//                                    Icon(
//                                        imageVector = when (player.roundRole) {
//                                            ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Speaker -> HalfHatIcon.OnlineGameSpeakerIcon
//                                            ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Listener -> HalfHatIcon.OnlineGameListenerIcon
//                                            ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Player -> HalfHatIcon.OnlineGamePlayerIcon
//                                        },
//                                        modifier = Modifier.size(24.dp),
//                                        contentDescription = null,
//                                    )
//                                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = word,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                    )
                    val allSeconds = spentTime.inWholeSeconds
                    val seconds = allSeconds % 60
                    val minutes = allSeconds / 60
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "$minutes:${seconds.toString().padStart(2, '0')}",
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}