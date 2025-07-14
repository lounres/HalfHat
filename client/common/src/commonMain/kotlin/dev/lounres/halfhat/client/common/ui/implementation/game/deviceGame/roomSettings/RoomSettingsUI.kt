package dev.lounres.halfhat.client.common.ui.implementation.game.deviceGame.roomSettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.applyDeviceGameSettingsButton_dark_png_24dp
import dev.lounres.halfhat.client.common.resources.discardDeviceGameSettingsButton_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomSettings.RoomSettingsComponent
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.state.subscribeAsState
import org.jetbrains.compose.resources.painterResource
import kotlin.math.min


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
                    component.preparationTimeSeconds.value =
                        input.filter { it.isDigit() }.let { if (it.isEmpty()) "" else min(it.dropWhile { d -> d == '0' }.ifBlank { "0" }.toUInt(), 999u).toString() }
                },
                label = {
                    Text(
                        text = "Countdown duration",
                    )
                },
                singleLine = true,
                isError = preparationTime.isBlank(),
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = explanationTime,
                onValueChange = { input ->
                    component.explanationTimeSeconds.value =
                        input.filter { it.isDigit() }.let { if (it.isEmpty()) "" else min(it.dropWhile { d -> d == '0' }.ifBlank { "0" }.toUInt(), 999u).toString() }
                },
                label = {
                    Text(
                        text = "Explanation duration",
                    )
                },
                singleLine = true,
                isError = explanationTime.isBlank(),
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = lastGuessTime,
                onValueChange = { input ->
                    component.finalGuessTimeSeconds.value =
                        input.filter { it.isDigit() }.let { if (it.isEmpty()) "" else min(it.dropWhile { d -> d == '0' }.ifBlank { "0" }.toUInt(), 999u).toString() }
                },
                label = {
                    Text(
                        text = "Last guess duration",
                    )
                },
                singleLine = true,
                isError = lastGuessTime.isBlank(),
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
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.cachedEndConditionWordsNumber.collectAsState().value,
                        onValueChange = { input ->
                            component.cachedEndConditionWordsNumber.value =
                                input.filter { it.isDigit() }.let { if (it.isEmpty()) "" else it.dropWhile { d -> d == '0' }.ifBlank { "0" } }
                        },
                        label = {
                            Text(
                                text = "The number of words",
                            )
                        },
                        singleLine = true,
                    )
                }
                
                GameStateMachine.GameEndCondition.Type.Cycles -> {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.cachedEndConditionCyclesNumber.collectAsState().value,
                        onValueChange = { input ->
                            component.cachedEndConditionCyclesNumber.value =
                                input.filter { it.isDigit() }.let { if (it.isEmpty()) "" else min(it.dropWhile { d -> d == '0' }.ifBlank { "0" }.toUInt(), 99u).toString() }
                        },
                        label = {
                            Text(
                                text = "The number of cycles",
                            )
                        },
                        singleLine = true,
                    )
                }
            }
        }
    }
}