package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.RoundScreenComponent
import dev.lounres.halfhat.client.ui.icons.*
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundEditing.RoundEditingGameCardUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundExplanation.RoundExplanationGameCardUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundLastGuess.RoundLastGuessGameCardUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundPreparation.RoundPreparationGameCardUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundWaiting.RoundWaitingGameCardUI
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.hub.set
import dev.lounres.kone.hub.subscribeAsState
import kotlinx.coroutines.launch


fun RoundScreenToolbarContentUI(
    component: RoundScreenComponent,
): @Composable RowScope.() -> Unit = {
    IconButton(
        onClick = component.onExitOnlineGame
    ) {
        Icon(
            modifier = commonIconModifier,
            imageVector = HalfHatIcon.OnlineGameExitRoomButton,
            contentDescription = "Exit online game room"
        )
    }
    IconButton(
        onClick = component.onCopyOnlineGameKey
    ) {
        Icon(
            modifier = commonIconModifier,
            imageVector = HalfHatIcon.OnlineGameCopyKeyButton,
            contentDescription = "Copy online game room key"
        )
    }
    IconButton(
        onClick = component.onCopyOnlineGameLink
    ) {
        Icon(
            modifier = commonIconModifier,
            imageVector = HalfHatIcon.OnlineGameCopyLinkButton,
            contentDescription = "Copy online game room link"
        )
    }
    
    when (val role = component.gameState.collectAsState().value.role) {
        is ServerApi.OnlineGame.Role.Round.Waiting ->
            if (role.isGameFinishable)
                IconButton(
                    onClick = component.onFinishGame
                ) {
                    Icon(
                        modifier = commonIconModifier,
                        imageVector = HalfHatIcon.OnlineGameFinishGameButton,
                        contentDescription = "Finish online game"
                    )
                }
        is ServerApi.OnlineGame.Role.Round.Preparation -> {}
        is ServerApi.OnlineGame.Role.Round.Explanation -> {}
        is ServerApi.OnlineGame.Role.Round.LastGuess -> {}
        is ServerApi.OnlineGame.Role.Round.Editing -> {}
    }
}

val toolbarColors @Composable get() = FloatingToolbarDefaults.vibrantFloatingToolbarColors()

@Composable
fun RoundScreenGameCardUI(
    component: RoundScreenComponent,
    modifier: Modifier,
) {
    Card(
        modifier = modifier,
    ) {
        when (val child = component.childSlot.subscribeAsState().value.component) {
            is RoundScreenComponent.Child.RoundWaiting -> RoundWaitingGameCardUI(child.component)
            is RoundScreenComponent.Child.RoundPreparation -> RoundPreparationGameCardUI(child.component)
            is RoundScreenComponent.Child.RoundExplanation -> RoundExplanationGameCardUI(child.component)
            is RoundScreenComponent.Child.RoundLastGuess -> RoundLastGuessGameCardUI(child.component)
            is RoundScreenComponent.Child.RoundEditing -> RoundEditingGameCardUI(child.component)
        }
    }
}

private fun ToggleButtonShapes.toIconToggleButtonShapes(): IconToggleButtonShapes =
    IconToggleButtonShapes(
        shape = shape,
        pressedShape = pressedShape,
        checkedShape = checkedShape,
    )

@Composable
fun RoundScreenAdditionalCardUI(
    component: RoundScreenComponent,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        val additionalCard = component.additionalCard.subscribeAsState().value
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            IconToggleButton(
                checked = additionalCard == RoundScreenComponent.AdditionalCard.Schedule,
                onCheckedChange = {
                    if (it) component.coroutineScope.launch {
                        component.additionalCard.set(RoundScreenComponent.AdditionalCard.Schedule)
                    }
                },
                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes().toIconToggleButtonShapes(),
                colors = IconButtonDefaults.filledTonalIconToggleButtonColors(),
            ) {
                Icon(
                    modifier = commonIconModifier,
                    imageVector = HalfHatIcon.OnlineGameScheduleButton,
                    contentDescription = "Open schedule",
                )
            }
            IconToggleButton(
                checked = additionalCard == RoundScreenComponent.AdditionalCard.PlayersStatistic,
                onCheckedChange = {
                    if (it) component.coroutineScope.launch {
                        component.additionalCard.set(RoundScreenComponent.AdditionalCard.PlayersStatistic)
                    }
                },
                shapes = ButtonGroupDefaults.connectedMiddleButtonShapes().toIconToggleButtonShapes(),
                colors = IconButtonDefaults.filledTonalIconToggleButtonColors(),
            ) {
                Icon(
                    modifier = commonIconModifier,
                    imageVector = HalfHatIcon.OnlineGamePlayersButton,
                    contentDescription = "Open players statistic",
                )
            }
            IconToggleButton(
                checked = additionalCard == RoundScreenComponent.AdditionalCard.WordsStatistic,
                onCheckedChange = {
                    if (it) component.coroutineScope.launch {
                        component.additionalCard.set(RoundScreenComponent.AdditionalCard.WordsStatistic)
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
                checked = additionalCard == RoundScreenComponent.AdditionalCard.Settings,
                onCheckedChange = {
                    if (it) component.coroutineScope.launch {
                        component.additionalCard.set(RoundScreenComponent.AdditionalCard.Settings)
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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val gameState = component.gameState.collectAsState().value
                when (additionalCard) {
                    RoundScreenComponent.AdditionalCard.Schedule -> {
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "You will be speaking in ${gameState.role.roundsBeforeSpeaking} rounds",
                            fontSize = 20.sp,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You will be listening in ${gameState.role.roundsBeforeListening} rounds",
                            fontSize = 20.sp,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${gameState.restWordsNumber + gameState.wordsInProgressNumber} (-${gameState.wordsInProgressNumber}) of ${gameState.initialWordsNumber} words left in the game",
                            fontSize = 20.sp,
                        )
                    }
                    RoundScreenComponent.AdditionalCard.PlayersStatistic -> {
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
                        for (index in gameState.leaderboardPermutation) {
                            val player = gameState.playersList[index]
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
                                        imageVector = when (player.roundRole) {
                                            ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Speaker -> HalfHatIcon.OnlineGameSpeakerIcon
                                            ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Listener -> HalfHatIcon.OnlineGameListenerIcon
                                            ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Player -> HalfHatIcon.OnlineGamePlayerIcon
                                        },
                                        modifier = Modifier.size(24.dp),
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = player.name,
                                    )
                                    val additionalPointsString =
                                        when (player.roundRole) {
                                            ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Speaker -> " (+${gameState.wordsInProgressNumber})"
                                            ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Listener -> " (+${gameState.wordsInProgressNumber})"
                                            ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Player -> ""
                                        }
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = "${player.scoreExplained}${if (player.roundRole == ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Speaker) additionalPointsString else ""}",
                                        textAlign = TextAlign.Center,
                                    )
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = "${player.scoreGuessed}${if (player.roundRole == ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Listener) additionalPointsString else ""}",
                                        textAlign = TextAlign.Center,
                                    )
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = "${player.scoreSum}$additionalPointsString",
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                    RoundScreenComponent.AdditionalCard.WordsStatistic -> {
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
                    RoundScreenComponent.AdditionalCard.Settings -> {
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
        }
    }
}

@Composable
fun ColumnScope.RoundScreenCompactUI(
    component: RoundScreenComponent,
) {
    val openAdditionalCard = component.openAdditionalCard.subscribeAsState().value
    HorizontalFloatingToolbar(
        colors = toolbarColors,
        expanded = true,
        floatingActionButton = {
            ToggleFloatingActionButton(
                checked = openAdditionalCard,
                onCheckedChange = {
                    component.coroutineScope.launch {
                        component.openAdditionalCard.set(it)
                    }
                },
            ) {
                val contentColor = lerp(
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.onPrimary,
                    checkedProgress,
                )
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    Icon(
                        modifier = commonIconModifier,
                        imageVector = HalfHatIcon.OnlineGameOpenAdditionalCardButton,
                        contentDescription = if (openAdditionalCard) "Close settings" else "Open settings"
                    )
                }
            }
        },
        content = RoundScreenToolbarContentUI(component),
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    val cardModifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().weight(1f)
    if (openAdditionalCard) RoundScreenAdditionalCardUI(component, cardModifier)
    else RoundScreenGameCardUI(component, cardModifier)
}

@Composable
fun ColumnScope.RoundScreenLargeUI(
    component: RoundScreenComponent,
) {
    val openAdditionalCard = component.openAdditionalCard.subscribeAsState().value
    HorizontalFloatingToolbar(
        colors = toolbarColors,
        expanded = true,
        floatingActionButton = {
            ToggleFloatingActionButton(
                checked = openAdditionalCard,
                onCheckedChange = {
                    component.coroutineScope.launch {
                        component.openAdditionalCard.set(it)
                    }
                },
            ) {
                val contentColor = lerp(
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.onPrimary,
                    checkedProgress,
                )
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    Icon(
                        modifier = commonIconModifier,
                        imageVector = HalfHatIcon.OnlineGameOpenAdditionalCardButton,
                        contentDescription = if (openAdditionalCard) "Close settings" else "Open settings"
                    )
                }
            }
        },
        content = RoundScreenToolbarContentUI(component),
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalArrangement = Arrangement.Center,
    ) {
        val cardModifier = Modifier.fillMaxHeight().weight(1f, false).widthIn(max = 420.dp)
        RoundScreenGameCardUI(component, cardModifier)
        if (openAdditionalCard) {
            Spacer(modifier = Modifier.width(32.dp))
            RoundScreenAdditionalCardUI(component, cardModifier)
        }
    }
}

@Composable
public fun RoundScreenUI(
    component: RoundScreenComponent,
    windowSizeClass: WindowSizeClass,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = component.gameState.collectAsState().value.roomName,
            fontSize = 48.sp,
        )
        val minWidthDp = windowSizeClass.minWidthDp
        when {
            minWidthDp >= WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND -> RoundScreenLargeUI(component) // Extra-large width
            minWidthDp >= WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND -> RoundScreenLargeUI(component) // Large width
            minWidthDp >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND -> RoundScreenCompactUI(component) // Expanded width
            minWidthDp >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND -> RoundScreenCompactUI(component) // Medium width
            else -> RoundScreenCompactUI(component) // Compact width
        }
    }
}