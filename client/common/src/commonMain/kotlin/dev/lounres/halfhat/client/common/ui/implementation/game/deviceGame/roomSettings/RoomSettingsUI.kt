package dev.lounres.halfhat.client.common.ui.implementation.game.deviceGame.roomSettings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.applyDeviceGameSettingsButton_dark_png_24dp
import dev.lounres.halfhat.client.common.resources.discardDeviceGameSettingsButton_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomSettings.RoomSettingsComponent
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.iterables.next
import org.jetbrains.compose.resources.painterResource


@Composable
public fun RowScope.RoomSettingsActionsUI(
    component: RoomSettingsComponent
) {
    IconButton(
        onClick = component.onApplySettings
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(Res.drawable.applyDeviceGameSettingsButton_dark_png_24dp),
            contentDescription = "Apply settings"
        )
    }
    IconButton(
        onClick = component.onDiscardSettings
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(Res.drawable.discardDeviceGameSettingsButton_dark_png_24dp),
            contentDescription = "Discard settings"
        )
    }
}

@Composable
public fun RoomSettingsUI(
    component: RoomSettingsComponent
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().widthIn(max = 480.dp).align(Alignment.Center).padding(8.dp)
        ) {
            val preparationTime = component.preparationTimeSeconds.collectAsState().value
            val explanationTime = component.explanationTimeSeconds.collectAsState().value
            val lastGuessTime = component.finalGuessTimeSeconds.collectAsState().value
            
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = preparationTime,
                onValueChange = { input ->
                    component.showErrorForPreparationTimeSeconds.value = false
                    component.preparationTimeSeconds.value =
                        input.filter { it.isDigit() }.let { if (it.isEmpty()) "" else it.dropWhile { d -> d == '0' }.ifBlank { "0" }.let { if (it.length > 3) "999" else it } }
                },
                label = {
                    Text(
                        text = "Countdown duration",
                    )
                },
                singleLine = true,
                isError = preparationTime.isBlank() && component.showErrorForPreparationTimeSeconds.collectAsState().value,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = explanationTime,
                onValueChange = { input ->
                    component.showErrorForExplanationTimeSeconds.value = false
                    component.explanationTimeSeconds.value =
                        input.filter { it.isDigit() }.let { if (it.isEmpty()) "" else it.dropWhile { d -> d == '0' }.ifBlank { "0" }.let { if (it.length > 3) "999" else it } }
                },
                label = {
                    Text(
                        text = "Explanation duration",
                    )
                },
                singleLine = true,
                isError = explanationTime.isBlank() && component.showErrorForExplanationTimeSeconds.collectAsState().value,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = lastGuessTime,
                onValueChange = { input ->
                    component.showErrorForFinalGuessTimeSeconds.value = false
                    component.finalGuessTimeSeconds.value =
                        input.filter { it.isDigit() }.let { if (it.isEmpty()) "" else it.dropWhile { d -> d == '0' }.ifBlank { "0" }.let { if (it.length > 3) "999" else it } }
                },
                label = {
                    Text(
                        text = "Last guess duration",
                    )
                },
                singleLine = true,
                isError = lastGuessTime.isBlank() && component.showErrorForFinalGuessTimeSeconds.collectAsState().value,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Strict mode",
                    fontSize = 16.sp,
                )
                Switch(
                    checked = component.strictMode.collectAsState().value,
                    onCheckedChange = { component.strictMode.value = it },
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            var dictionaryMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                modifier = Modifier.fillMaxWidth(),
                expanded = false,
                onExpandedChange = { dictionaryMenuExpanded = it },
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    value = when (val wordsSource = component.wordsSource.collectAsState().value) {
                        GameStateMachine.WordsSource.Players -> error("For some reason from-each-player dictionary is chosen")
                        is GameStateMachine.WordsSource.Custom -> "Custom: ${wordsSource.providerId.name}"
                    },
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = {
                        Text(text = "Words source")
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dictionaryMenuExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                )
                ExposedDropdownMenu(
                    expanded = dictionaryMenuExpanded,
                    onDismissRequest = { dictionaryMenuExpanded = false },
                ) {
//                    DropdownMenuItem(
//                        text = { Text(text = "From each player") },
//                        onClick = {
//                            component.wordsSource.value = GameStateMachine.WordsSource.Players
//                            dictionaryMenuExpanded = false
//                        },
//                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
//                    )
//                    HorizontalDivider()
                    for (id in component.possibleWordsSources)
                        DropdownMenuItem(
                            text = { Text(text = id.name) },
                            onClick = {
                                component.wordsSource.value = GameStateMachine.WordsSource.Custom(id)
                                dictionaryMenuExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            var gameEndConditionMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                modifier = Modifier.fillMaxWidth(),
                expanded = false,
                onExpandedChange = { gameEndConditionMenuExpanded = it },
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    value = when (component.gameEndConditionType.collectAsState().value) {
                        GameStateMachine.GameEndCondition.Type.Words -> "Words"
                        GameStateMachine.GameEndCondition.Type.Cycles -> "Cycles"
                    },
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = {
                        Text(text = "Game end condition")
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gameEndConditionMenuExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                )
                ExposedDropdownMenu(
                    expanded = gameEndConditionMenuExpanded,
                    onDismissRequest = { gameEndConditionMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "Words") },
                        onClick = {
                            component.gameEndConditionType.value = GameStateMachine.GameEndCondition.Type.Words
                            gameEndConditionMenuExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Cycles") },
                        onClick = {
                            component.gameEndConditionType.value = GameStateMachine.GameEndCondition.Type.Cycles
                            gameEndConditionMenuExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (component.gameEndConditionType.collectAsState().value) {
                GameStateMachine.GameEndCondition.Type.Words -> {
                    val currentInput = component.cachedEndConditionWordsNumber.collectAsState().value
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = currentInput,
                        onValueChange = { input ->
                            component.showErrorForCachedEndConditionWordsNumber.value = false
                            component.cachedEndConditionWordsNumber.value =
                                input.filter { it.isDigit() }.let { if (it.isEmpty()) "" else it.dropWhile { d -> d == '0' }.ifBlank { "0" } }
                        },
                        label = {
                            Text(
                                text = "The number of words",
                            )
                        },
                        singleLine = true,
                        isError = (currentInput.isEmpty() || currentInput == "0") && component.showErrorForCachedEndConditionWordsNumber.collectAsState().value,
                    )
                }
                
                GameStateMachine.GameEndCondition.Type.Cycles -> {
                    val currentInput = component.cachedEndConditionCyclesNumber.collectAsState().value
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = currentInput,
                        onValueChange = { input ->
                            component.showErrorForCachedEndConditionCyclesNumber.value = false
                            component.cachedEndConditionCyclesNumber.value =
                                input.filter { it.isDigit() }.let { if (it.isEmpty()) "" else it.dropWhile { d -> d == '0' }.ifBlank { "0" }.let { if (it.length > 2) "99" else it } }
                        },
                        label = {
                            Text(
                                text = "The number of cycles",
                            )
                        },
                        singleLine = true,
                        isError = (currentInput.isEmpty() || currentInput == "0") && component.showErrorForCachedEndConditionCyclesNumber.collectAsState().value,
                    )
                }
            }
        }
    }
}