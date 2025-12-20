package dev.lounres.halfhat.client.ui.implementation.game.controller.roomSettings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.lounres.halfhat.client.resources.Res
import dev.lounres.halfhat.client.resources.applyDeviceGameSettingsButton_dark_png_24dp
import dev.lounres.halfhat.client.resources.discardDeviceGameSettingsButton_dark_png_24dp
import dev.lounres.halfhat.client.ui.components.game.controller.roomSettings.RoomSettingsComponent
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
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
            
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
//                Text(
//                    text = "Strict mode",
//                    fontSize = 16.sp,
//                )
//                Switch(
//                    checked = component.strictMode.collectAsState().value,
//                    onCheckedChange = { component.strictMode.value = it },
//                )
//            }
        }
    }
}