package dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roomSettings

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
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roomSettings.RoomSettingsComponent
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import org.jetbrains.compose.resources.painterResource


@Composable
public fun RowScope.RoomSettingsActionsUI(
    component: RoomSettingsComponent
) {

}

@Composable
public fun ColumnScope.RoomSettingsUI(
    component: RoomSettingsComponent
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp)
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = component.preparationTimeSeconds.collectAsState().value.toString(),
            onValueChange = {
                component.preparationTimeSeconds.value =
                    it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" }.toUInt()
            },
            label = {
                Text(
                    text = "Readiness time",
                )
            },
            singleLine = true,
        )
        
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

@Composable
public fun RowScope.RoomSettingsButtonsUI(
    component: RoomSettingsComponent
) {
    IconButton(
        onClick = component.onApplySettings
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(Res.drawable.applyDeviceGameSettingsButton_dark_png_24dp), // TODO: Copy the icons
            contentDescription = "Apply settings"
        )
    }
    IconButton(
        onClick = component.onDiscardSettings
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(Res.drawable.discardDeviceGameSettingsButton_dark_png_24dp), // TODO: Copy the icons
            contentDescription = "Discard settings"
        )
    }
}