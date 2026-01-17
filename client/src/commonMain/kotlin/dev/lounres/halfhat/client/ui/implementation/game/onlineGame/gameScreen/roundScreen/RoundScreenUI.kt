package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.IconToggleButtonShapes
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.RoundScreenComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameCopyKeyButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameCopyLinkButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameExitRoomButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameFinishGameButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameHostMarkIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameListenerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameOpenAdditionalCardButton
import dev.lounres.halfhat.client.ui.icons.OnlineGamePlayerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGamePlayersButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameScheduleButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameSettingsButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameSpeakerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameSpeakerToListenerRightArrow
import dev.lounres.halfhat.client.ui.icons.OnlineGameWordsButton
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundEditing.RoundEditingGameCardUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundExplanation.RoundExplanationGameCardUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundLastGuess.RoundLastGuessGameCardUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundPreparation.RoundPreparationGameCardUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundWaiting.RoundWaitingGameCardUI
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.kone.collections.list.indices
import dev.lounres.kone.hub.subscribeAsState


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

private enum class AdditionalCard {
    Schedule, PlayersStatistic, WordsStatistic, Settings,
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
        var additionalCard by remember { mutableStateOf(AdditionalCard.Schedule) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            IconToggleButton(
                checked = additionalCard == AdditionalCard.Schedule,
                onCheckedChange = { if (it) additionalCard = AdditionalCard.Schedule },
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
                checked = additionalCard == AdditionalCard.PlayersStatistic,
                onCheckedChange = { if (it) additionalCard = AdditionalCard.PlayersStatistic },
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
                enabled = false,
                checked = additionalCard == AdditionalCard.WordsStatistic,
                onCheckedChange = { if (it) additionalCard = AdditionalCard.WordsStatistic },
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
                checked = additionalCard == AdditionalCard.Settings,
                onCheckedChange = { if (it) additionalCard = AdditionalCard.Settings },
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
                    AdditionalCard.Schedule -> {
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
                                        text = "?", // TODO
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
                                        text = "?", // TODO
                                        autoSize = TextAutoSize.StepBased(maxFontSize = 32.sp),
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "You are speaker in ?? rounds", // TODO
                            fontSize = 20.sp,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You are listener in ?? rounds", // TODO
                            fontSize = 20.sp,
                        )
                    }
                    AdditionalCard.PlayersStatistic -> {
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
                        // TODO: Move the leaderboard to the server logic
                        val playersList = gameState.playersList
                        for (index in playersList.indices.sortedByDescending { gameState.explanationScores[it] + gameState.guessingScores[it] }) {
                            val player = playersList[index]
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
                                        imageVector = when (index) {
                                            gameState.speakerIndex -> HalfHatIcon.OnlineGameSpeakerIcon
                                            gameState.listenerIndex -> HalfHatIcon.OnlineGameListenerIcon
                                            else -> HalfHatIcon.OnlineGamePlayerIcon
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
                                        if (index == gameState.speakerIndex || index == gameState.listenerIndex) " (+0)"
                                        else ""
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = "${gameState.explanationScores[index]}${if (index == gameState.speakerIndex) additionalPointsString else ""}",
                                        textAlign = TextAlign.Center,
                                    )
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = "${gameState.guessingScores[index]}${if (index == gameState.listenerIndex) additionalPointsString else ""}",
                                        textAlign = TextAlign.Center,
                                    )
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = "${gameState.explanationScores[index] + gameState.guessingScores[index]}$additionalPointsString",
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                    AdditionalCard.WordsStatistic -> {} // TODO
                    AdditionalCard.Settings -> {} // TODO
                }
            }
        }
    }
}

@Composable
fun ColumnScope.RoundScreenCompactUI(
    component: RoundScreenComponent,
) {
    var openAdditionalCard by remember { mutableStateOf(false) }
    HorizontalFloatingToolbar(
        colors = toolbarColors,
        expanded = true,
        floatingActionButton = {
            ToggleFloatingActionButton(
                checked = openAdditionalCard,
                onCheckedChange = { openAdditionalCard = it },
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
    var openAdditionalCard by remember { mutableStateOf(false) }
    HorizontalFloatingToolbar(
        colors = toolbarColors,
        expanded = true,
        floatingActionButton = {
            ToggleFloatingActionButton(
                checked = openAdditionalCard,
                onCheckedChange = { openAdditionalCard = it },
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
        val gameState = component.gameState.collectAsState().value
        Text(
            text = gameState.roomName,
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