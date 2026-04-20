package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.wordsCollection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import dev.lounres.halfhat.api.onlineGame.DictionaryId
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.wordsCollection.WordsCollectionComponent
import dev.lounres.halfhat.client.ui.icons.*
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.iterables.isNotEmpty
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.addAllFrom
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.utils.filter
import dev.lounres.kone.collections.utils.withIndex
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.set
import dev.lounres.kone.hub.subscribeAsState
import dev.lounres.kone.hub.update
import kotlinx.coroutines.launch


fun WordsCollectionToolbarContentUI(
    component: WordsCollectionComponent,
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
}

val toolbarColors @Composable get() = FloatingToolbarDefaults.vibrantFloatingToolbarColors()

@Composable
fun WordsCollectionGameCardUI(
    component: WordsCollectionComponent,
    modifier: Modifier,
) {
    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val gameState = component.gameState.collectAsState().value
            when (val globalRole = gameState.selfRole.globalRole) {
                is ServerApi.OnlineGame.SelfRole.PlayersWordsCollection.GlobalRole.Player ->
                    if (globalRole.finishedWordsCollection) {
                        Text(
                            text = "Your words are submitted successfully",
                            fontSize = 36.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 40.sp,
                        )
                        Text(
                            text = "You can see submitting status of other players in players menu",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top,
                        ) {
                            val currentWords = component.currentWords.subscribeAsState().value
                            for ((val index, val word = value) in currentWords.withIndex()) {
                                if (index != 0u) Spacer(modifier = Modifier.height(16.dp))
                                key(word) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            modifier = Modifier.width(36.dp),
                                            text = (index + 1u).toString(),
                                            textAlign = TextAlign.End,
                                            maxLines = 1,
                                            softWrap = false,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                            modifier = Modifier.weight(1f),
                                            value = word.subscribeAsState().value,
                                            onValueChange = {
                                                component.coroutineScope.launch {
                                                    word.set(it)
                                                }
                                            },
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                component.coroutineScope.launch {
                                                    component.currentWords.update { words ->
                                                        words.filter { it !== word }
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = HalfHatIcon.OnlineGamePlayersWordsRemoveWord,
                                                contentDescription = "Remove word",
                                            )
                                        }
                                    }
                                }
                            }
                            if (currentWords.isNotEmpty()) Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    component.coroutineScope.launch {
                                        component.currentWords.update { words ->
                                            KoneList.build {
                                                addAllFrom(words)
                                                +KoneMutableAsynchronousHub("")
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = HalfHatIcon.OnlineGamePlayersWordsAddWord,
                                    modifier = Modifier.size(24.dp),
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Add new word",
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CircleShape,
                            onClick = component.onSubmit,
                        ) {
                            Text(
                                text = "Submit",
                                fontSize = 32.sp,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                ServerApi.OnlineGame.SelfRole.PlayersWordsCollection.GlobalRole.Spectator ->
                    Text(
                        text = "You can see submitting status of other players in players menu",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                    )
            }
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
fun WordsCollectionAdditionalCardUI(
    component: WordsCollectionComponent,
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
                checked = additionalCard == WordsCollectionComponent.AdditionalCard.PlayersReadiness,
                onCheckedChange = {
                    if (it) component.coroutineScope.launch {
                        component.additionalCard.set(WordsCollectionComponent.AdditionalCard.PlayersReadiness)
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
                checked = additionalCard == WordsCollectionComponent.AdditionalCard.Settings,
                onCheckedChange = {
                    if (it) component.coroutineScope.launch {
                        component.additionalCard.set(WordsCollectionComponent.AdditionalCard.Settings)
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
                    WordsCollectionComponent.AdditionalCard.PlayersReadiness -> {
                        val players = gameState.playersList.withIndex().filter { it.value.globalRole is ServerApi.OnlineGame.PlayerDescription.PlayersWordsCollection.GlobalRole.Player }
                        val nonPlayers = gameState.playersList.withIndex().filter { it.value.globalRole !is ServerApi.OnlineGame.PlayerDescription.PlayersWordsCollection.GlobalRole.Player }
                        for ((val index, val indexedPlayer = value) in players.withIndex()) {
                            (val userIndex = index, val player = value) = indexedPlayer
                            val globalRole = player.globalRole as ServerApi.OnlineGame.PlayerDescription.PlayersWordsCollection.GlobalRole.Player
                            if (index != 0u) Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = CircleShape,
                                color =
                                    if (userIndex == gameState.selfRole.userIndex) {
                                        if (globalRole.finishedWordsCollection) MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.tertiaryContainer
                                    } else {
                                        if (globalRole.finishedWordsCollection) MaterialTheme.colorScheme.inverseSurface
                                        else MaterialTheme.colorScheme.surface
                                    },
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
                                        imageVector = HalfHatIcon.OnlineGamePlayerIcon,
                                        modifier = Modifier.size(24.dp),
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = player.name,
                                    )
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
                                                is ServerApi.OnlineGame.PlayerDescription.PlayersWordsCollection.GlobalRole.Player -> error(TODO())
                                                is ServerApi.OnlineGame.PlayerDescription.PlayersWordsCollection.GlobalRole.Spectator -> HalfHatIcon.OnlineGameSpectatorIcon
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
                    WordsCollectionComponent.AdditionalCard.Settings -> {
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
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            value = when (val wordsSource = settingsBuilder.wordsSource) {
                                ServerApi.WordsSource.Players -> "Players"
                                ServerApi.WordsSource.HostDictionary -> "Host dictionary"
                                is ServerApi.WordsSource.ServerDictionary -> when (val description = wordsSource.dictionaryIdWithDescription) {
                                    is DictionaryId.WithDescription.Builtin -> description.name
                                }
                            },
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            label = {
                                Text(
                                    text = "Words source",
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                enabled = false,
                                checked = settingsBuilder.strictMode,
                                onCheckedChange = {},
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
fun ColumnScope.WordsCollectionCompactUI(
    component: WordsCollectionComponent,
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
        content = WordsCollectionToolbarContentUI(component),
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    val cardModifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().weight(1f)
    if (openAdditionalCard) WordsCollectionAdditionalCardUI(component, cardModifier)
    else WordsCollectionGameCardUI(component, cardModifier)
}

@Composable
fun ColumnScope.WordsCollectionLargeUI(
    component: WordsCollectionComponent,
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
        content = WordsCollectionToolbarContentUI(component),
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalArrangement = Arrangement.Center,
    ) {
        val cardModifier = Modifier.fillMaxHeight().weight(1f, false).widthIn(max = 420.dp)
        WordsCollectionGameCardUI(component, cardModifier)
        if (openAdditionalCard) {
            Spacer(modifier = Modifier.width(32.dp))
            WordsCollectionAdditionalCardUI(component, cardModifier)
        }
    }
}

@Composable
fun WordsCollectionUI(
    component: WordsCollectionComponent,
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
            minWidthDp >= WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND -> WordsCollectionLargeUI(component) // Extra-large width
            minWidthDp >= WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND -> WordsCollectionLargeUI(component) // Large width
            minWidthDp >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND -> WordsCollectionCompactUI(component) // Expanded width
            minWidthDp >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND -> WordsCollectionCompactUI(component) // Medium width
            else -> WordsCollectionCompactUI(component) // Compact width
        }
    }
}