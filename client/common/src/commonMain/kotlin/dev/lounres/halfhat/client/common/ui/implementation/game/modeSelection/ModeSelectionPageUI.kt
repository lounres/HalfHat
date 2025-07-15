package dev.lounres.halfhat.client.common.ui.implementation.game.modeSelection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.lounres.halfhat.client.common.resources.*
import dev.lounres.halfhat.client.common.ui.components.game.modeSelection.ModeSelectionPageComponent
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import dev.lounres.kone.hub.subscribeAsState
import dev.lounres.kone.maybe.ifSome
import org.jetbrains.compose.resources.painterResource


@Composable
public fun ColumnScope.ModeSelectionPageUI(
    component: ModeSelectionPageComponent,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        component.infoPopup.subscribeAsState().value.ifSome {
            Dialog(
                onDismissRequest = component.onCloseInfo
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text =
                                when (it.component) {
                                    ModeSelectionPageComponent.InfoPopup.OnlineGame -> "This is a mode that provides a game over internet by official HalfHat server."
                                    ModeSelectionPageComponent.InfoPopup.LocalGame -> "This is a mode that provides a game over local network by HalfHat application on some device."
                                    ModeSelectionPageComponent.InfoPopup.DeviceGame -> "This is a mode that provides a game on this device."
                                    ModeSelectionPageComponent.InfoPopup.GameTimer -> "This is a mode that provides only a timer for the game."
                                },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = component.onCloseInfo,
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = component.onOnlineGameSelect,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.onlineGamePage_dark_png_24dp),
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
            ) {
                Icon(
                    painter = painterResource(Res.drawable.gameModeDescriptionButton_dark_png_24dp),
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
                    painter = painterResource(Res.drawable.localGamePage_dark_png_24dp),
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
            ) {
                Icon(
                    painter = painterResource(Res.drawable.gameModeDescriptionButton_dark_png_24dp),
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
                    painter = painterResource(Res.drawable.deviceGamePage_dark_png_24dp),
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
            ) {
                Icon(
                    painter = painterResource(Res.drawable.gameModeDescriptionButton_dark_png_24dp),
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
                    painter = painterResource(Res.drawable.timerPage_dark_png_24dp),
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
            ) {
                Icon(
                    painter = painterResource(Res.drawable.gameModeDescriptionButton_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = "Game timer description",
                )
            }
        }
    }
}