package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roomScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roomScreen.RoomScreenComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameCopyKeyButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameCopyLinkButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameExitRoomButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameHostMarkIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGamePlayerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameSettingsButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameSettingsIconBetweenTimes
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.utils.withIndex
import kotlin.text.ifEmpty


fun RoomScreenToolbarContentUI(
    component: RoomScreenComponent,
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
fun RoomScreenRoomCardUI(
    component: RoomScreenComponent,
    modifier: Modifier,
) {
    val gameState = component.gameState.collectAsState().value
    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val playersList = gameState.playersList
            for ((index, player) in playersList.withIndex()) {
                if (index != 0u) Spacer(modifier = Modifier.height(8.dp))
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
                            imageVector = HalfHatIcon.OnlineGamePlayerIcon,
                            modifier = Modifier.size(24.dp),
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = player.name)
                    }
                }
            }
        }
    }
}

@Composable
fun RoomScreenSettingsCardUI(
    component: RoomScreenComponent,
    modifier: Modifier,
) {
    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val gameState = component.gameState.collectAsState().value
            val settingsBuilder = gameState.settingsBuilder
            val areSettingsChangeable = gameState.role.areSettingsChangeable
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                val currentPreparationTimeSeconds = component.preparationTimeSeconds.collectAsState().value
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    enabled = areSettingsChangeable,
                    isError = areSettingsChangeable && currentPreparationTimeSeconds != null,
                    value = (currentPreparationTimeSeconds.takeIf { areSettingsChangeable } ?: settingsBuilder.preparationTimeSeconds).toString(),
                    onValueChange = {
                        component.preparationTimeSeconds.value =
                            it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" }.toUInt()
                    },
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
                
                val currentExplanationTimeSeconds = component.explanationTimeSeconds.collectAsState().value
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    enabled = areSettingsChangeable,
                    isError = areSettingsChangeable && currentExplanationTimeSeconds != null,
                    value = (currentExplanationTimeSeconds.takeIf { areSettingsChangeable } ?: settingsBuilder.explanationTimeSeconds).toString(),
                    onValueChange = {
                        component.explanationTimeSeconds.value =
                            it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" }.toUInt()
                    },
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
                
                val currentFinalGuessTimeSeconds = component.finalGuessTimeSeconds.collectAsState().value
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    enabled = areSettingsChangeable,
                    isError = areSettingsChangeable && currentFinalGuessTimeSeconds != null,
                    value = (currentFinalGuessTimeSeconds.takeIf { areSettingsChangeable } ?: settingsBuilder.finalGuessTimeSeconds).toString(),
                    onValueChange = {
                        component.finalGuessTimeSeconds.value =
                            it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" }.toUInt()
                    },
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
                var menuExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    modifier = Modifier.weight(1f),
                    expanded = false,
                    onExpandedChange = { menuExpanded = it },
                ) {
                    val currentGameEndConditionType = component.gameEndConditionType.collectAsState().value
                    val actualGameEndConditionType = currentGameEndConditionType.takeIf { areSettingsChangeable } ?: settingsBuilder.gameEndConditionType
                    val isChanged = areSettingsChangeable && currentGameEndConditionType != null
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, areSettingsChangeable),
                        enabled = areSettingsChangeable,
                        isError = isChanged,
                        value = when (actualGameEndConditionType) {
                            GameStateMachine.GameEndCondition.Type.Words -> "Words"
                            GameStateMachine.GameEndCondition.Type.Cycles -> "Cycles"
                        },
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = {
                            Text(
                                text = "Game end condition",
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            errorCursorColor = MaterialTheme.colorScheme.tertiary,
                            errorBorderColor = MaterialTheme.colorScheme.tertiary,
                            errorTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                            errorLabelColor = MaterialTheme.colorScheme.tertiary,
//                            errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                    
                    ExposedDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        val itemColors =
                            if (isChanged)
                                MenuDefaults.selectableItemColors()
                            else
                                MenuDefaults.selectableItemColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.onSecondary,
                                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary,
                                    selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondary,
                                )
                        DropdownMenuItem(
                            selected = actualGameEndConditionType == GameStateMachine.GameEndCondition.Type.Words,
                            text = { Text(text = "Words", style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                component.gameEndConditionType.value = GameStateMachine.GameEndCondition.Type.Words
                                menuExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            shapes = MenuDefaults.itemShapes(),
                            colors = itemColors,
                        )
                        DropdownMenuItem(
                            selected = actualGameEndConditionType == GameStateMachine.GameEndCondition.Type.Cycles,
                            text = { Text(text = "Cycles", style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                component.gameEndConditionType.value = GameStateMachine.GameEndCondition.Type.Cycles
                                menuExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            shapes = MenuDefaults.itemShapes(),
                            colors = itemColors,
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                when (
                    component.gameEndConditionType.collectAsState().value.takeIf { areSettingsChangeable } ?: settingsBuilder.gameEndConditionType
                ) {
                    GameStateMachine.GameEndCondition.Type.Words -> {
                        val currentCachedEndConditionWordsNumber = component.cachedEndConditionWordsNumber.collectAsState().value
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            enabled = areSettingsChangeable,
                            isError = areSettingsChangeable && currentCachedEndConditionWordsNumber != null,
                            value = (currentCachedEndConditionWordsNumber.takeIf { areSettingsChangeable } ?: settingsBuilder.cachedEndConditionWordsNumber).toString(),
                            onValueChange = {
                                component.cachedEndConditionWordsNumber.value =
                                    it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" }.toUInt()
                            },
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
                    
                    GameStateMachine.GameEndCondition.Type.Cycles -> {
                        val currentCachedEndConditionCyclesNumber = component.cachedEndConditionCyclesNumber.collectAsState().value
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            enabled = areSettingsChangeable,
                            isError = areSettingsChangeable && currentCachedEndConditionCyclesNumber != null,
                            value = (currentCachedEndConditionCyclesNumber.takeIf { areSettingsChangeable } ?: settingsBuilder.cachedEndConditionCyclesNumber).toString(),
                            onValueChange = {
                                component.cachedEndConditionCyclesNumber.value =
                                    it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" }.toUInt()
                            },
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
                val currentStrictMode = component.strictMode.collectAsState().value
                Checkbox(
                    enabled = areSettingsChangeable,
                    checked = currentStrictMode.takeIf { areSettingsChangeable } ?: settingsBuilder.strictMode,
                    onCheckedChange = { component.strictMode.value = it },
                    colors =
                        if (areSettingsChangeable && currentStrictMode != null)
                            CheckboxDefaults.colors(
                                checkedCheckmarkColor = MaterialTheme.colorScheme.onTertiary,
                                checkedBoxColor = MaterialTheme.colorScheme.tertiary,
                                checkedBorderColor = MaterialTheme.colorScheme.tertiary,
                                uncheckedBorderColor = MaterialTheme.colorScheme.tertiary,
                            )
                        else
                            CheckboxDefaults.colors()
                )
                
                Text(
                    text = "Strict mode",
                    fontSize = 20.sp,
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (
                areSettingsChangeable && (
                    component.preparationTimeSeconds.collectAsState().value != null ||
                    component.explanationTimeSeconds.collectAsState().value != null ||
                    component.finalGuessTimeSeconds.collectAsState().value != null ||
                    component.strictMode.collectAsState().value != null ||
                    component.cachedEndConditionWordsNumber.collectAsState().value != null ||
                    component.cachedEndConditionCyclesNumber.collectAsState().value != null ||
                    component.gameEndConditionType.collectAsState().value != null
                )
            ) Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = component.onApplySettings
                ) {
                    Text(
                        text = "Apply",
                        fontSize = 24.sp,
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                FilledTonalButton(
                    onClick = component.onDiscardSettings
                ) {
                    Text(
                        text = "Discard",
                        fontSize = 24.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun ColumnScope.RoomScreenCompactUI(
    component: RoomScreenComponent,
) {
    var openSettings by remember { mutableStateOf(false) }
    HorizontalFloatingToolbar(
        colors = toolbarColors,
        expanded = true,
        floatingActionButton = {
            ToggleFloatingActionButton(
                checked = openSettings,
                onCheckedChange = { openSettings = it },
            ) {
                val contentColor = lerp(
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.onPrimary,
                    checkedProgress,
                )
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    Icon(
                        modifier = commonIconModifier,
                        imageVector = HalfHatIcon.OnlineGameSettingsButton,
                        contentDescription = if (openSettings) "Close settings" else "Open settings"
                    )
                }
            }
        },
        content = RoomScreenToolbarContentUI(component),
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    val cardModifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().weight(1f)
    if (openSettings) RoomScreenSettingsCardUI(component, cardModifier)
    else RoomScreenRoomCardUI(component, cardModifier)
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(
        enabled = component.gameState.collectAsState().value.role.isStartAvailable,
        onClick = component.onStartGame
    ) {
        Text("START", fontSize = 32.sp)
    }
}

@Composable
fun ColumnScope.RoomScreenLargeUI(
    component: RoomScreenComponent,
) {
    var openSettings by remember { mutableStateOf(false) }
    HorizontalFloatingToolbar(
        colors = toolbarColors,
        expanded = true,
        floatingActionButton = {
            ToggleFloatingActionButton(
                checked = openSettings,
                onCheckedChange = { openSettings = it },
            ) {
                val contentColor = lerp(
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.onPrimary,
                    checkedProgress,
                )
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    Icon(
                        modifier = commonIconModifier,
                        imageVector = HalfHatIcon.OnlineGameSettingsButton,
                        contentDescription = if (openSettings) "Close settings" else "Open settings"
                    )
                }
            }
        },
        content = RoomScreenToolbarContentUI(component),
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalArrangement = Arrangement.Center,
    ) {
        val cardModifier = Modifier.fillMaxHeight().weight(1f, false).widthIn(max = 420.dp)
        RoomScreenRoomCardUI(component, cardModifier)
        if (openSettings) {
            Spacer(modifier = Modifier.width(32.dp))
            RoomScreenSettingsCardUI(component, cardModifier)
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(
        enabled = component.gameState.collectAsState().value.role.isStartAvailable,
        onClick = component.onStartGame
    ) {
        Text("START", fontSize = 32.sp)
    }
}

@Composable
public fun RoomScreenUI(
    component: RoomScreenComponent,
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
            minWidthDp >= WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND -> RoomScreenLargeUI(component) // Extra-large width
            minWidthDp >= WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND -> RoomScreenLargeUI(component) // Large width
            minWidthDp >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND -> RoomScreenCompactUI(component) // Expanded width
            minWidthDp >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND -> RoomScreenCompactUI(component) // Medium width
            else -> RoomScreenCompactUI(component) // Compact width
        }
    }
}