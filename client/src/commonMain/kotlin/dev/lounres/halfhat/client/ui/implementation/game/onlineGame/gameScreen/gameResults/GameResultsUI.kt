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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.gameResults.GameResultsComponent
import dev.lounres.halfhat.client.ui.icons.*
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.utils.withIndex
import dev.lounres.kone.hub.set
import dev.lounres.kone.hub.subscribeAsState
import kotlinx.coroutines.launch


private fun ToggleButtonShapes.toIconToggleButtonShapes(): IconToggleButtonShapes =
    IconToggleButtonShapes(
        shape = shape,
        pressedShape = pressedShape,
        checkedShape = checkedShape,
    )

@Composable
public fun GameResultsUI(
    component: GameResultsComponent,
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
            val section = component.section.subscribeAsState().value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                IconToggleButton(
                    checked = section == GameResultsComponent.Section.PlayersStatistic,
                    onCheckedChange = {
                        if (it) component.coroutineScope.launch {
                            component.section.set(GameResultsComponent.Section.PlayersStatistic)
                        }
                    },
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
                    checked = section == GameResultsComponent.Section.WordsStatistic,
                    onCheckedChange = {
                        if (it) component.coroutineScope.launch {
                            component.section.set(GameResultsComponent.Section.WordsStatistic)
                        }
                    },
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
                    checked = section == GameResultsComponent.Section.Settings,
                    onCheckedChange = {
                        if (it) component.coroutineScope.launch {
                            component.section.set(GameResultsComponent.Section.Settings)
                        }
                    },
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
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val gameState = component.gameState.collectAsState().value
                        when (section) {
                            GameResultsComponent.Section.PlayersStatistic -> {
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
//                                            if (player.isHost)
//                                                Icon(
//                                                    imageVector = HalfHatIcon.OnlineGameHostMarkIcon,
//                                                    modifier = Modifier.size(24.dp),
//                                                    contentDescription = null,
//                                                )
//                                            else
//                                                Spacer(Modifier.width(24.dp))
//                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                imageVector = HalfHatIcon.OnlineGamePlayerIcon,
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
                                                text = "${player.scoreExplained}",
                                                textAlign = TextAlign.Center,
                                            )
                                            Text(
                                                modifier = Modifier.weight(1f),
                                                text = "${player.scoreExplained}",
                                                textAlign = TextAlign.Center,
                                            )
                                            Text(
                                                modifier = Modifier.weight(1f),
                                                text = "${player.scoreSum}",
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }
                            }
                            
                            GameResultsComponent.Section.WordsStatistic -> {
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
                                        text = "Spent time",
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
                                Column(
                                    modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                                ) {
                                    val useDark = component.darkTheme.subscribeAsState().value.isDark
                                    for ((word, spentTime, state) in gameState.wordsStatistic) {
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
//                                                Icon(
//                                                    imageVector = when (player.roundRole) {
//                                                        ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Speaker -> HalfHatIcon.OnlineGameSpeakerIcon
//                                                        ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Listener -> HalfHatIcon.OnlineGameListenerIcon
//                                                        ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Player -> HalfHatIcon.OnlineGamePlayerIcon
//                                                    },
//                                                    modifier = Modifier.size(24.dp),
//                                                    contentDescription = null,
//                                                )
//                                                Spacer(modifier = Modifier.width(16.dp))
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
                            GameResultsComponent.Section.Settings -> {
                                val gameState = component.gameState.collectAsState().value
                                val settingsBuilder = gameState.settings
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Bottom,
                                ) {
                                    OutlinedTextField(
                                        modifier = Modifier.weight(1f),
                                        enabled = false,
                                        value = settingsBuilder.preparationTimeSeconds.toString(),
                                        onValueChange = {},
                                        label = {
                                            Text(
                                                text = "Preparation",
                                            )
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(textAlign = TextAlign.Center),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            errorCursorColor = MaterialTheme.colorScheme.tertiary,
                                            errorBorderColor = MaterialTheme.colorScheme.tertiary,
                                            errorTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                                            errorLabelColor = MaterialTheme.colorScheme.tertiary,
                                            errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
                                        ),
                                    )
                                    
                                    Column {
                                        Icon(
                                            modifier = commonIconModifier,
                                            imageVector = HalfHatIcon.OnlineGameSettingsIconBetweenTimes,
                                            contentDescription = null,
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                    
                                    OutlinedTextField(
                                        modifier = Modifier.weight(1f),
                                        enabled = false,
                                        value = settingsBuilder.explanationTimeSeconds.toString(),
                                        onValueChange = {},
                                        label = {
                                            Text(
                                                text = "Explanation",
                                            )
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(textAlign = TextAlign.Center),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            errorCursorColor = MaterialTheme.colorScheme.tertiary,
                                            errorBorderColor = MaterialTheme.colorScheme.tertiary,
                                            errorTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                                            errorLabelColor = MaterialTheme.colorScheme.tertiary,
                                            errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
                                        ),
                                    )
                                    
                                    Column {
                                        Icon(
                                            modifier = commonIconModifier,
                                            imageVector = HalfHatIcon.OnlineGameSettingsIconBetweenTimes,
                                            contentDescription = null,
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                    
                                    OutlinedTextField(
                                        modifier = Modifier.weight(1f),
                                        enabled = false,
                                        value = settingsBuilder.finalGuessTimeSeconds.toString(),
                                        onValueChange = {},
                                        label = {
                                            Text(
                                                text = "Final guess",
                                            )
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(textAlign = TextAlign.Center),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            errorCursorColor = MaterialTheme.colorScheme.tertiary,
                                            errorBorderColor = MaterialTheme.colorScheme.tertiary,
                                            errorTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                                            errorLabelColor = MaterialTheme.colorScheme.tertiary,
                                            errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
                                        ),
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Bottom,
                                ) {
                                    val actualGameEndConditionType = settingsBuilder.gameEndCondition
                                    OutlinedTextField(
                                        modifier = Modifier.weight(1f),
                                        enabled = false,
                                        value = when (actualGameEndConditionType) {
                                            is GameStateMachine.GameEndCondition.Words -> "Words"
                                            is GameStateMachine.GameEndCondition.Cycles -> "Cycles"
                                        },
                                        onValueChange = {},
                                        readOnly = true,
                                        singleLine = true,
                                        label = {
                                            Text(
                                                text = "Game end condition",
                                            )
                                        },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                            errorCursorColor = MaterialTheme.colorScheme.tertiary,
                                            errorBorderColor = MaterialTheme.colorScheme.tertiary,
                                            errorTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                                            errorLabelColor = MaterialTheme.colorScheme.tertiary,
//                                        errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
                                        ),
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    when (val gameEndCondition = settingsBuilder.gameEndCondition) {
                                        is GameStateMachine.GameEndCondition.Words -> {
                                            OutlinedTextField(
                                                modifier = Modifier.weight(1f),
                                                enabled = false,
                                                value = gameEndCondition.number.toString(),
                                                onValueChange = {},
                                                label = {
                                                    Text(
                                                        text = "The number of words",
                                                    )
                                                },
                                                textStyle = TextStyle(textAlign = TextAlign.Center),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    errorCursorColor = MaterialTheme.colorScheme.tertiary,
                                                    errorBorderColor = MaterialTheme.colorScheme.tertiary,
                                                    errorTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                                                    errorLabelColor = MaterialTheme.colorScheme.tertiary,
                                                    errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
                                                ),
                                            )
                                        }
                                        
                                        is GameStateMachine.GameEndCondition.Cycles -> {
                                            OutlinedTextField(
                                                modifier = Modifier.weight(1f),
                                                enabled = false,
                                                value = gameEndCondition.number.toString(),
                                                onValueChange = {},
                                                label = {
                                                    Text(
                                                        text = "The number of cycles",
                                                    )
                                                },
                                                textStyle = TextStyle(textAlign = TextAlign.Center),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    errorCursorColor = MaterialTheme.colorScheme.tertiary,
                                                    errorBorderColor = MaterialTheme.colorScheme.tertiary,
                                                    errorTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                                                    errorLabelColor = MaterialTheme.colorScheme.tertiary,
                                                    errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
                                                ),
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        enabled = false,
                                        checked = settingsBuilder.strictMode,
                                        onCheckedChange = {},
                                        colors = CheckboxDefaults.colors()
                                    )
                                    
                                    Text(
                                        text = "Strict mode",
                                        fontSize = 20.sp,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = component.onLeaveGameResults,
                        ) {
                            Text(
                                text = "Leave room",
                                fontSize = 16.sp,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button( // TODO
                            enabled = false,
                            modifier = Modifier.weight(1f),
                            onClick = {},
                        ) {
                            Text(
                                text = "Play again",
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}