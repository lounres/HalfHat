package dev.lounres.halfhat.client.ui.implementation.game.modeSelection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.window.core.layout.WindowSizeClass
import dev.lounres.halfhat.client.resources.*
import dev.lounres.halfhat.client.ui.components.game.modeSelection.ModeSelectionPageComponent
import dev.lounres.halfhat.client.ui.icons.GameModeDescriptionButton
import dev.lounres.halfhat.client.ui.icons.GameModeDeviceGameIcon
import dev.lounres.halfhat.client.ui.icons.GameModeGameControllerIcon
import dev.lounres.halfhat.client.ui.icons.GameModeGameTimerIcon
import dev.lounres.halfhat.client.ui.icons.GameModeLocalGameIcon
import dev.lounres.halfhat.client.ui.icons.GameModeOnlineGameIcon
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.kone.hub.subscribeAsState
import dev.lounres.kone.maybe.ifSome
import org.jetbrains.compose.resources.painterResource


@Composable
public fun ModeSelectionPageUI(
    component: ModeSelectionPageComponent,
    windowSizeClass: WindowSizeClass,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.widthIn(max = 480.dp).padding(16.dp).align(Alignment.Center),
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
                                        ModeSelectionPageComponent.InfoPopup.GameController -> "This is a mode that provides a timer and a controller of playing players for the game."
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
                        imageVector = HalfHatIcon.GameModeOnlineGameIcon,
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
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = HalfHatIcon.GameModeDescriptionButton,
                        modifier = commonIconModifier,
                        contentDescription = "Online game description",
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    enabled = false,
                    modifier = Modifier.weight(1f),
                    onClick = component.onLocalGameSelect,
                ) {
                    Icon(
                        imageVector = HalfHatIcon.GameModeLocalGameIcon,
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
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = HalfHatIcon.GameModeDescriptionButton,
                        modifier = commonIconModifier,
                        contentDescription = "Local game description",
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    enabled = false,
                    modifier = Modifier.weight(1f),
                    onClick = component.onDeviceGameSelect,
                ) {
                    Icon(
                        imageVector = HalfHatIcon.GameModeDeviceGameIcon,
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
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = HalfHatIcon.GameModeDescriptionButton,
                        modifier = commonIconModifier,
                        contentDescription = "Device game description",
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    enabled = false,
                    modifier = Modifier.weight(1f),
                    onClick = component.onGameControllerSelect,
                ) {
                    Icon(
                        imageVector = HalfHatIcon.GameModeGameControllerIcon,
                        modifier = commonIconModifier,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Game controller"
                    )
                }
                IconButton(
                    onClick = component.onGameControllerInfo,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = HalfHatIcon.GameModeDescriptionButton,
                        modifier = commonIconModifier,
                        contentDescription = "Game controller description",
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    enabled = false,
                    modifier = Modifier.weight(1f),
                    onClick = component.onGameTimerSelect,
                ) {
                    Icon(
                        imageVector = HalfHatIcon.GameModeGameTimerIcon,
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
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = HalfHatIcon.GameModeDescriptionButton,
                        modifier = commonIconModifier,
                        contentDescription = "Game timer description",
                    )
                }
            }
        }
    }
}