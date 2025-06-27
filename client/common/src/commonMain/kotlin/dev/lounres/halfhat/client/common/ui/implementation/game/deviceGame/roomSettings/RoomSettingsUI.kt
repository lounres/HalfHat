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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = component.preparationTimeSeconds.collectAsState().value.toString(),
                onValueChange = {
                    component.preparationTimeSeconds.value =
                        (it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" }).toUInt()
                },
                label = {
                    Text(
                        text = "Readiness time",
                    )
                },
                singleLine = true,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = component.explanationTimeSeconds.collectAsState().value.toString(),
                onValueChange = {
                    component.explanationTimeSeconds.value =
                        it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" }.toUInt()
                },
                label = {
                    Text(
                        text = "Readiness time",
                    )
                },
                singleLine = true,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = component.finalGuessTimeSeconds.collectAsState().value.toString(),
                onValueChange = {
                    component.finalGuessTimeSeconds.value =
                        it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" }.toUInt()
                },
                label = {
                    Text(
                        text = "Readiness time",
                    )
                },
                singleLine = true,
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
            
            var menuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                modifier = Modifier.fillMaxWidth(),
                expanded = false,
                onExpandedChange = { menuExpanded = it },
            ) {
                TextField(
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                )
                ExposedDropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "Words", style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            component.gameEndConditionType.value = GameStateMachine.GameEndCondition.Type.Words
                            menuExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Cycles") },
                        onClick = {
                            component.gameEndConditionType.value = GameStateMachine.GameEndCondition.Type.Cycles
                            menuExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (component.gameEndConditionType.collectAsState().value) {
                GameStateMachine.GameEndCondition.Type.Words -> {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.cachedEndConditionWordsNumber.collectAsState().value.toString(),
                        onValueChange = {
                            component.cachedEndConditionWordsNumber.value =
                                it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" }.toUInt()
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
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.cachedEndConditionCyclesNumber.collectAsState().value.toString(),
                        onValueChange = {
                            component.cachedEndConditionWordsNumber.value =
                                it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" }.toUInt()
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