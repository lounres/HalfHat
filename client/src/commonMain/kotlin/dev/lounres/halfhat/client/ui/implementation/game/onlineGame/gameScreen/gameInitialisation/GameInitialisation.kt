package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.gameInitialisation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import dev.lounres.halfhat.api.onlineGame.DictionaryId
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.gameInitialisation.GameInitialisationComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameCopyKeyButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameCopyLinkButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameExitRoomButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameGameInitialisationPlayerMenuClose
import dev.lounres.halfhat.client.ui.icons.OnlineGameGameInitialisationPlayerMenuOpen
import dev.lounres.halfhat.client.ui.icons.OnlineGameHostMarkIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGamePlayerIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameSettingsButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameSettingsIconBetweenTimes
import dev.lounres.halfhat.client.ui.icons.OnlineGameSpectatorIcon
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.interop.toKoneList
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.utils.withIndex
import dev.lounres.kone.scope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.launch
import kotlin.text.ifEmpty


fun GameInitialisationToolbarContentUI(
    component: GameInitialisationComponent,
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
fun GameInitialisationRoomCardUI(
    component: GameInitialisationComponent,
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
            for ((val index, val player = value) in playersList.withIndex()) {
                if (index != 0u) Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color =
                        if (index == gameState.selfRole.userIndex) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                    ) {
                        var descriptionIsOpen by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                imageVector = when (player.globalRole) {
                                    is ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Player -> HalfHatIcon.OnlineGamePlayerIcon
                                    is ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Spectator -> HalfHatIcon.OnlineGameSpectatorIcon
                                },
                                modifier = Modifier.size(24.dp),
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = player.name)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { descriptionIsOpen = !descriptionIsOpen },
                            ) {
                                Icon(
                                    imageVector =
                                        if (descriptionIsOpen) HalfHatIcon.OnlineGameGameInitialisationPlayerMenuOpen
                                        else HalfHatIcon.OnlineGameGameInitialisationPlayerMenuClose,
                                    modifier = Modifier.size(24.dp),
                                    contentDescription = null,
                                )
                            }
                        }
                        if (descriptionIsOpen) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val areSettingsChangeable = gameState.selfRole.areSettingsChangeable
                            var roleSelectionIsExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                modifier = Modifier.fillMaxWidth(),
                                expanded = false,
                                onExpandedChange = { roleSelectionIsExpanded = it },
                            ) {
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, areSettingsChangeable),
                                    enabled = areSettingsChangeable,
                                    value = when (player.globalRole) {
                                        is ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Player -> "Player"
                                        is ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Spectator -> "Spectator"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    label = {
                                        Text(
                                            text = "Role",
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when (player.globalRole) {
                                                is ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Player -> HalfHatIcon.OnlineGamePlayerIcon
                                                is ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Spectator -> HalfHatIcon.OnlineGameSpectatorIcon
                                            },
                                            modifier = Modifier.size(24.dp),
                                            contentDescription = null,
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector =
                                                if (roleSelectionIsExpanded) HalfHatIcon.OnlineGameGameInitialisationPlayerMenuOpen
                                                else HalfHatIcon.OnlineGameGameInitialisationPlayerMenuClose,
                                            modifier = Modifier.size(24.dp),
                                            contentDescription = null,
                                        )
                                    },
                                )

                                ExposedDropdownMenu(
                                    expanded = roleSelectionIsExpanded,
                                    onDismissRequest = { roleSelectionIsExpanded = false },
                                ) {
                                    val itemColors = MenuDefaults.selectableItemColors()
                                    DropdownMenuItem(
                                        selected = player.globalRole is ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Player,
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                Icon(
                                                    imageVector = HalfHatIcon.OnlineGamePlayerIcon,
                                                    modifier = Modifier.size(24.dp),
                                                    contentDescription = null,
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = "Player", style = MaterialTheme.typography.bodyLarge)
                                            }
                                        },
                                        onClick = {
                                            component.onUpdateRoles(index, ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Player)
                                            roleSelectionIsExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                        shapes = MenuDefaults.itemShapes(),
                                        colors = itemColors,
                                    )
                                    DropdownMenuItem(
                                        selected = player.globalRole is ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Spectator,
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                Icon(
                                                    imageVector = HalfHatIcon.OnlineGameSpectatorIcon,
                                                    modifier = Modifier.size(24.dp),
                                                    contentDescription = null,
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = "Spectator", style = MaterialTheme.typography.bodyLarge)
                                            }
                                        },
                                        onClick = {
                                            component.onUpdateRoles(index, ServerApi.OnlineGame.PlayerDescription.GameInitialisation.GlobalRole.Spectator)
                                            roleSelectionIsExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                        shapes = MenuDefaults.itemShapes(),
                                        colors = itemColors,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameInitialisationSettingsCardUI(
    component: GameInitialisationComponent,
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
            val extraSettings = gameState.extraSettings
            val areSettingsChangeable = gameState.selfRole.areSettingsChangeable
            
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
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
                    var gameEndConditionMenuExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        modifier = Modifier.weight(1f),
                        expanded = false,
                        onExpandedChange = { gameEndConditionMenuExpanded = it },
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
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gameEndConditionMenuExpanded) },
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
                            expanded = gameEndConditionMenuExpanded,
                            onDismissRequest = { gameEndConditionMenuExpanded = false },
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
                                    gameEndConditionMenuExpanded = false
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
                                    gameEndConditionMenuExpanded = false
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
                
                scope {
                    var wordSourceMenuExpanded by remember { mutableStateOf(false) }
                    
                    val currentWordSource = component.wordsSource.collectAsState().value
                    val actualWordSource = currentWordSource.takeIf { areSettingsChangeable }
                        ?: when (val wordsSource = settingsBuilder.wordsSource) {
                            ServerApi.WordsSource.Players -> GameInitialisationComponent.WordsSource.Players
                            ServerApi.WordsSource.HostDictionary -> GameInitialisationComponent.WordsSource.HostDictionary
                            is ServerApi.WordsSource.ServerDictionary -> GameInitialisationComponent.WordsSource.ServerDictionary(wordsSource.dictionaryIdWithDescription)
                        }
                    val isChanged = areSettingsChangeable && currentWordSource != null
                    
                    ExposedDropdownMenuBox(
                        modifier = Modifier.fillMaxWidth(),
                        expanded = false,
                        onExpandedChange = { wordSourceMenuExpanded = it },
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, areSettingsChangeable),
                            enabled = areSettingsChangeable,
                            isError = isChanged,
                            value = when (actualWordSource) {
                                GameInitialisationComponent.WordsSource.Players -> "Players"
                                GameInitialisationComponent.WordsSource.HostDictionary -> "Host dictionary"
                                is GameInitialisationComponent.WordsSource.ServerDictionary -> when (val description = actualWordSource.description) {
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
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wordSourceMenuExpanded) },
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                errorCursorColor = MaterialTheme.colorScheme.tertiary,
                                errorBorderColor = MaterialTheme.colorScheme.tertiary,
                                errorTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                                errorLabelColor = MaterialTheme.colorScheme.tertiary,
        //                        errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                        
                        ExposedDropdownMenu(
                            expanded = wordSourceMenuExpanded,
                            onDismissRequest = { wordSourceMenuExpanded = false },
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
                                selected = actualWordSource == GameInitialisationComponent.WordsSource.Players,
                                text = { Text(text = "Players", style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    component.wordsSource.value = GameInitialisationComponent.WordsSource.Players
                                    wordSourceMenuExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                shapes = MenuDefaults.itemShapes(),
                                colors = itemColors,
                            )
                            DropdownMenuItem(
                                selected = actualWordSource == GameInitialisationComponent.WordsSource.HostDictionary,
                                text = { Text(text = "Host dictionary", style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    component.wordsSource.value = GameInitialisationComponent.WordsSource.HostDictionary
                                    wordSourceMenuExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                shapes = MenuDefaults.itemShapes(),
                                colors = itemColors,
                            )
                            val serverDictionaries by component.availableDictionaries.collectAsState()
                            LaunchedEffect(Unit) {
                                component.onLoadServerDictionaries()
                            }
                            serverDictionaries.let { serverDictionaries ->
                                if (serverDictionaries == null) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        ContainedLoadingIndicator(
                                            modifier = Modifier.align(Alignment.Center),
                                        )
                                    }
                                } else {
                                    for (description in serverDictionaries) when (description) {
                                        is DictionaryId.WithDescription.Builtin ->
                                            DropdownMenuItem(
                                                selected = actualWordSource is GameInitialisationComponent.WordsSource.ServerDictionary && actualWordSource.description.id == description.id,
                                                text = {
                                                    Column(
                                                        modifier = Modifier.fillMaxWidth(),
                                                    ) {
                                                        Text(text = description.name, style = MaterialTheme.typography.bodyLarge)
                                                        Text(text = "${description.wordsNumber} words", style = MaterialTheme.typography.bodyMedium)
                                                    }
                                                },
                                                onClick = {
                                                    component.wordsSource.value = GameInitialisationComponent.WordsSource.ServerDictionary(description)
                                                    wordSourceMenuExpanded = false
                                                },
                                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                                shapes = MenuDefaults.itemShapes(),
                                                colors = itemColors,
                                            )
                                    }
                                }
                            }
                        }
                    }
                    
                    if (actualWordSource is GameInitialisationComponent.WordsSource.HostDictionary) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val coroutineScope = rememberCoroutineScope()
                        val currentHostDictionary = component.hostDictionary.collectAsState().value
                        val contentColor = when {
                            currentHostDictionary != null -> MaterialTheme.colorScheme.tertiary
                            currentWordSource != null -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .aspectRatio(2f)
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .drawBehind {
                                    drawRoundRect(
                                        color = contentColor,
                                        style = Stroke(
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 8.dp.toPx())),
                                        ),
                                        cornerRadius = CornerRadius(8.dp.toPx()),
                                    )
                                }
                                .clickable(
                                    onClick = {
                                        coroutineScope.launch {
                                            val file = FileKit.openFilePicker(
                                                type = FileKitType.File("txt")
                                            )
                                            if (file != null) {
                                                component.hostDictionary.value = file.readString().lines().toKoneList()
                                            }
                                        }
                                    }
                                )
                                /*.dragAndDropTarget( // TODO: Wait for support of drag and drop in Compose
                                    shouldStartDragAndDrop = { true },
                                    target = remember {
                                        object: DragAndDropTarget {
                                            
                                            // Highlights the border of a potential drop target
                                            override fun onEntered(event: DragAndDropEvent) {
                                                // TODO
                                            }
                                            
                                            override fun onExited(event: DragAndDropEvent) {
                                                // TODO
                                            }
                                            
                                            override fun onDrop(event: DragAndDropEvent): Boolean {
                                                return true
                                            }
                                        }
                                    }
                                )*/,
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Click to open file",
                                fontSize = 16.sp,
                                style = TextStyle(color = contentColor),
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val currentShowLeaderboardPermutation = component.showLeaderboardPermutation.collectAsState().value
                    Checkbox(
                        enabled = areSettingsChangeable,
                        checked = currentShowLeaderboardPermutation.takeIf { areSettingsChangeable } ?: extraSettings.showLeaderboardPermutation,
                        onCheckedChange = { component.showLeaderboardPermutation.value = it },
                        colors =
                            if (areSettingsChangeable && currentShowLeaderboardPermutation != null)
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
                        text = "Show leaderboard",
                        fontSize = 20.sp,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val currentShowWordsStatistic = component.showWordsStatistic.collectAsState().value
                    Checkbox(
                        enabled = areSettingsChangeable,
                        checked = currentShowWordsStatistic.takeIf { areSettingsChangeable } ?: extraSettings.showWordsStatistic,
                        onCheckedChange = { component.showWordsStatistic.value = it },
                        colors =
                            if (areSettingsChangeable && currentShowWordsStatistic != null)
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
                        text = "Show words statistics",
                        fontSize = 20.sp,
                    )
                }
            }
            
            if (
                areSettingsChangeable && (
                    component.preparationTimeSeconds.collectAsState().value != null ||
                    component.explanationTimeSeconds.collectAsState().value != null ||
                    component.finalGuessTimeSeconds.collectAsState().value != null ||
                    component.strictMode.collectAsState().value != null ||
                    component.cachedEndConditionWordsNumber.collectAsState().value != null ||
                    component.cachedEndConditionCyclesNumber.collectAsState().value != null ||
                    component.gameEndConditionType.collectAsState().value != null ||
                    component.wordsSource.collectAsState().value != null ||
                    component.showWordsStatistic.collectAsState().value != null ||
                    component.showLeaderboardPermutation.collectAsState().value != null
                )
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
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
}

@Composable
fun ColumnScope.GameInitialisationCompactUI(
    component: GameInitialisationComponent,
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
        content = GameInitialisationToolbarContentUI(component),
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    val cardModifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().weight(1f)
    if (openSettings) GameInitialisationSettingsCardUI(component, cardModifier)
    else GameInitialisationRoomCardUI(component, cardModifier)
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(
        enabled = component.gameState.collectAsState().value.selfRole.isStartAvailable,
        onClick = component.onStartGame
    ) {
        Text("START", fontSize = 32.sp)
    }
}

@Composable
fun ColumnScope.GameInitialisationLargeUI(
    component: GameInitialisationComponent,
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
        content = GameInitialisationToolbarContentUI(component),
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalArrangement = Arrangement.Center,
    ) {
        val cardModifier = Modifier.fillMaxHeight().weight(1f, false).widthIn(max = 420.dp)
        GameInitialisationRoomCardUI(component, cardModifier)
        if (openSettings) {
            Spacer(modifier = Modifier.width(32.dp))
            GameInitialisationSettingsCardUI(component, cardModifier)
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(
        enabled = component.gameState.collectAsState().value.selfRole.isStartAvailable,
        onClick = component.onStartGame
    ) {
        Text("START", fontSize = 32.sp)
    }
}

@Composable
public fun GameInitialisationUI(
    component: GameInitialisationComponent,
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
            minWidthDp >= WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND -> GameInitialisationLargeUI(component) // Extra-large width
            minWidthDp >= WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND -> GameInitialisationLargeUI(component) // Large width
            minWidthDp >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND -> GameInitialisationCompactUI(component) // Expanded width
            minWidthDp >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND -> GameInitialisationCompactUI(component) // Medium width
            else -> GameInitialisationCompactUI(component) // Compact width
        }
    }
}