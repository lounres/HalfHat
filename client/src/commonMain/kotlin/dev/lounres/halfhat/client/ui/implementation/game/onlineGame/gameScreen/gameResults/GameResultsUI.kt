package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.gameResults

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.gameResults.GameResultsComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameHostMarkIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameListenerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGamePlayerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGamePlayersButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameSettingsButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameSpeakerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameWordsButton
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.list.indices
import dev.lounres.kone.collections.utils.withIndex


private enum class ResultsSection {
    PlayersStatistic, WordsStatistic, Settings
}

private fun ToggleButtonShapes.toIconToggleButtonShapes(): IconToggleButtonShapes =
    IconToggleButtonShapes(
        shape = shape,
        pressedShape = pressedShape,
        checkedShape = checkedShape,
    )

@Composable
public fun GameResultsUI(
    component: GameResultsComponent
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val gameState = component.gameState.collectAsState().value
        Text(
            text = gameState.roomName,
            fontSize = 48.sp,
        )
        
        Column(
            modifier = Modifier.widthIn(max = 630.dp).fillMaxWidth().weight(1f),
        ) {
            var section by remember { mutableStateOf(ResultsSection.PlayersStatistic) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                IconToggleButton(
                    checked = section == ResultsSection.PlayersStatistic,
                    onCheckedChange = { if (it) section = ResultsSection.PlayersStatistic },
                    shapes = ButtonGroupDefaults.connectedLeadingButtonShapes().toIconToggleButtonShapes(),
                    colors = IconButtonDefaults.filledTonalIconToggleButtonColors(),
                ) {
                    Icon(
                        modifier = commonIconModifier,
                        imageVector = HalfHatIcon.OnlineGamePlayersButton,
                        contentDescription = "Open players statistic",
                    )
                }
                IconToggleButton(
                    enabled = false,
                    checked = section == ResultsSection.WordsStatistic,
                    onCheckedChange = { if (it) section = ResultsSection.WordsStatistic },
                    shapes = ButtonGroupDefaults.connectedMiddleButtonShapes().toIconToggleButtonShapes(),
                    colors = IconButtonDefaults.filledTonalIconToggleButtonColors(),
                ) {
                    Icon(
                        modifier = commonIconModifier,
                        imageVector = HalfHatIcon.OnlineGameWordsButton,
                        contentDescription = "Open words statistic",
                    )
                }
                IconToggleButton(
                    enabled = false,
                    checked = section == ResultsSection.Settings,
                    onCheckedChange = { if (it) section = ResultsSection.Settings },
                    shapes = ButtonGroupDefaults.connectedTrailingButtonShapes().toIconToggleButtonShapes(),
                    colors = IconButtonDefaults.filledTonalIconToggleButtonColors(),
                ) {
                    Icon(
                        modifier = commonIconModifier,
                        imageVector = HalfHatIcon.OnlineGameSettingsButton,
                        contentDescription = "Open game settings",
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val gameState = component.gameState.collectAsState().value
                    when (section) {
                        ResultsSection.PlayersStatistic -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                            ) {
                                Spacer(modifier = Modifier.width(40.dp))
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = "Player",
                                    fontWeight = FontWeight.SemiBold,
                                    autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp),
                                    softWrap = false,
                                    maxLines = 1,
                                )
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = "Explained",
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold,
                                    autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp),
                                    softWrap = false,
                                    maxLines = 1,
                                )
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = "Guessed",
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold,
                                    autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp),
                                    softWrap = false,
                                    maxLines = 1,
                                )
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = "Sum",
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold,
                                    autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp),
                                    softWrap = false,
                                    maxLines = 1,
                                )
                            }
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline,
                            )
                            val playersList = gameState.playersList
                            for ((index, player) in playersList.withIndex()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color =
                                        if (index == gameState.role.userIndex) MaterialTheme.colorScheme.tertiaryContainer
                                        else MaterialTheme.colorScheme.surface,
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
//                                        if (player.isHost)
//                                            Icon(
//                                                imageVector = HalfHatIcon.OnlineGameHostMarkIcon,
//                                                modifier = Modifier.size(24.dp),
//                                                contentDescription = null,
//                                            )
//                                        else
//                                            Spacer(Modifier.width(24.dp))
//                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            imageVector = HalfHatIcon.OnlineGamePlayerIcon,
                                            modifier = Modifier.size(24.dp),
                                            contentDescription = null,
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            modifier = Modifier.weight(1f),
                                            text = player,
                                        )
                                        Text(
                                            modifier = Modifier.weight(1f),
                                            text = "${gameState.results[index].scoreExplained}",
                                            textAlign = TextAlign.Center,
                                        )
                                        Text(
                                            modifier = Modifier.weight(1f),
                                            text = "${gameState.results[index].scoreExplained}",
                                            textAlign = TextAlign.Center,
                                        )
                                        Text(
                                            modifier = Modifier.weight(1f),
                                            text = "${gameState.results[index].sum}",
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                        ResultsSection.WordsStatistic -> {} // TODO
                        ResultsSection.Settings -> {} // TODO
                    }
                }
            }
        }
    }
}