package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.RoundScreenComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameHostMarkIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameListenerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGamePlayerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameSpeakerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameSpectatorIcon
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.utils.filter
import dev.lounres.kone.collections.utils.withIndex


@Composable
fun RoundScreenAdditionalCardPlayersStatisticUI(
    component: RoundScreenComponent,
    additionalCardChild: RoundScreenComponent.AdditionalCardChild.PlayersStatistic,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val gameState = component.gameState.collectAsState().value
        val players = gameState.playersList.withIndex().filter { it.value.globalRole is ServerApi.OnlineGame.PlayerDescription.Round.GlobalRole.Player }
        val nonPlayers = gameState.playersList.withIndex().filter { it.value.globalRole !is ServerApi.OnlineGame.PlayerDescription.Round.GlobalRole.Player }
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
        ) {
            Spacer(modifier = Modifier.width(68.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = "Player",
                fontWeight = FontWeight.SemiBold,
                autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp),
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "Explained",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp),
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "Guessed",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp),
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "Sum",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp),
            )
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
        )
        val leaderboard = additionalCardChild.leaderboard
        if (leaderboard != null) {
            for (index in additionalCardChild.leaderboard.permutation) {
                (val userIndex = index, val player = value) = players[index]
                val globalRole = player.globalRole as ServerApi.OnlineGame.PlayerDescription.Round.GlobalRole.Player
                val scoreExplained = leaderboard.scoreExplained[index]
                val scoreGuessed = leaderboard.scoreGuessed[index]
                val scoreSum = leaderboard.scoreSum[index]
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = CircleShape,
                    color =
                        if (userIndex == gameState.selfRole.userIndex) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (player.isHost)
                            Icon(
                                imageVector = HalfHatIcon.OnlineGameHostMarkIcon,
                                modifier = Modifier.size(24.dp),
                                contentDescription = null,
                            )
                        else
                            Spacer(Modifier.width(24.dp))
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = when (globalRole.roundRole) {
                                ServerApi.OnlineGame.PlayerDescription.Round.GlobalRole.Player.RoundRole.Speaker -> HalfHatIcon.OnlineGameSpeakerIcon
                                ServerApi.OnlineGame.PlayerDescription.Round.GlobalRole.Player.RoundRole.Listener -> HalfHatIcon.OnlineGameListenerIcon
                                null -> HalfHatIcon.OnlineGamePlayerIcon
                            },
                            modifier = Modifier.size(24.dp),
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = player.name,
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "$scoreExplained${if (globalRole.roundRole == ServerApi.OnlineGame.PlayerDescription.Round.GlobalRole.Player.RoundRole.Speaker) " (+${gameState.wordsInProgressNumber})" else ""}",
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "$scoreGuessed${if (globalRole.roundRole == ServerApi.OnlineGame.PlayerDescription.Round.GlobalRole.Player.RoundRole.Listener) " (+${gameState.wordsInProgressNumber})" else ""}",
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "$scoreSum${if (globalRole.roundRole == ServerApi.OnlineGame.PlayerDescription.Round.GlobalRole.Player.RoundRole.Speaker || globalRole.roundRole == ServerApi.OnlineGame.PlayerDescription.Round.GlobalRole.Player.RoundRole.Listener) " (+${gameState.wordsInProgressNumber})" else ""}",
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            for ((val player = value, val userIndex = index) in players) {
                val globalRole = player.globalRole as ServerApi.OnlineGame.PlayerDescription.Round.GlobalRole.Player
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = CircleShape,
                    color =
                        if (userIndex == gameState.selfRole.userIndex) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (player.isHost)
                            Icon(
                                imageVector = HalfHatIcon.OnlineGameHostMarkIcon,
                                modifier = Modifier.size(24.dp),
                                contentDescription = null,
                            )
                        else
                            Spacer(Modifier.width(24.dp))
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = when (globalRole.roundRole) {
                                ServerApi.OnlineGame.PlayerDescription.Round.GlobalRole.Player.RoundRole.Speaker -> HalfHatIcon.OnlineGameSpeakerIcon
                                ServerApi.OnlineGame.PlayerDescription.Round.GlobalRole.Player.RoundRole.Listener -> HalfHatIcon.OnlineGameListenerIcon
                                null -> HalfHatIcon.OnlineGamePlayerIcon
                            },
                            modifier = Modifier.size(24.dp),
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = player.name,
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "???",
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "???",
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "???",
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            for ((val player = value, val userIndex = index) in nonPlayers) {
                Surface(
                    modifier = Modifier.padding(4.dp),
                    shape = CircleShape,
                    color =
                        if (userIndex == gameState.selfRole.userIndex) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = when (player.globalRole) {
                                is ServerApi.OnlineGame.PlayerDescription.Round.GlobalRole.Player -> error(TODO())
                                is ServerApi.OnlineGame.PlayerDescription.Round.GlobalRole.Spectator -> HalfHatIcon.OnlineGameSpectatorIcon
                            },
                            modifier = Modifier.size(24.dp),
                            contentDescription = null,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            modifier = Modifier,
                            text = player.name,
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}