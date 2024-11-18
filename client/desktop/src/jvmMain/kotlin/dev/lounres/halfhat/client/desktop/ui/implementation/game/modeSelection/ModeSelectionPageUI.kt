package dev.lounres.halfhat.client.desktop.ui.implementation.game.modeSelection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.lounres.halfhat.client.desktop.resources.deviceGamePage_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.gameModeDescriptionButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.localGamePage_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.onlineGamePage_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.timerPage_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.game.modeSelection.ModeSelectionPageComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import org.jetbrains.compose.resources.painterResource


@Composable
fun ModeSelectionPageUI(
    component: ModeSelectionPageComponent,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = component.onOnlineGameSelect,
            ) {
                Icon(
                    painter = painterResource(DesktopRes.drawable.onlineGamePage_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Online game"
                )
            }
            IconButton(
                onClick = component.onOnlineGameInfo,
                enabled = false,
            ) {
                Icon(
                    painter = painterResource(DesktopRes.drawable.gameModeDescriptionButton_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = "Online game description",
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = component.onLocalGameSelect,
            ) {
                Icon(
                    painter = painterResource(DesktopRes.drawable.localGamePage_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Local game"
                )
            }
            IconButton(
                onClick = component.onLocalGameInfo,
                enabled = false,
            ) {
                Icon(
                    painter = painterResource(DesktopRes.drawable.gameModeDescriptionButton_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = "Local game description",
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = component.onDeviceGameSelect,
            ) {
                Icon(
                    painter = painterResource(DesktopRes.drawable.deviceGamePage_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Device game"
                )
            }
            IconButton(
                onClick = component.onDeviceGameInfo,
                enabled = false,
            ) {
                Icon(
                    painter = painterResource(DesktopRes.drawable.gameModeDescriptionButton_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = "Device game description",
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = component.onGameTimerSelect,
            ) {
                Icon(
                    painter = painterResource(DesktopRes.drawable.timerPage_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Game timer"
                )
            }
            IconButton(
                onClick = component.onGameTimerInfo,
                enabled = false,
            ) {
                Icon(
                    painter = painterResource(DesktopRes.drawable.gameModeDescriptionButton_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = "Game timer description",
                )
            }
        }
    }
}