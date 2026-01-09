package dev.lounres.halfhat.client.ui.implementation.game.deviceGame.roomScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.lounres.halfhat.client.resources.*
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.halfhat.client.ui.components.game.deviceGame.roomScreen.RoomScreenComponent
import dev.lounres.halfhat.client.ui.icons.DeviceGameAddPlayerButton
import dev.lounres.halfhat.client.ui.icons.DeviceGameExitModeButton
import dev.lounres.halfhat.client.ui.icons.DeviceGamePlayerIcon
import dev.lounres.halfhat.client.ui.icons.DeviceGameRemovePlayer
import dev.lounres.halfhat.client.ui.icons.DeviceGameSettingsButton
import dev.lounres.halfhat.client.ui.icons.DeviceGameShufflePlayersButton
import dev.lounres.halfhat.client.ui.icons.DeviceGameStartGameButton
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.utils.withIndex
import org.jetbrains.compose.resources.painterResource


@Composable
public fun RowScope.RoomScreenActionsUI(
    component: RoomScreenComponent,
) {
    IconButton(
        onClick = component.onExitDeviceGame
    ) {
        Icon(
            modifier = commonIconModifier,
            imageVector = HalfHatIcon.DeviceGameExitModeButton,
            contentDescription = "Exit device game"
        )
    }
}

@Composable
public fun RoomScreenUI(
    component: RoomScreenComponent,
) {
    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val playersList = component.playersList.collectAsState().value
        val showErrorForPlayers = component.showErrorForPlayers.collectAsState().value
        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 480.dp)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            for ((index, player) in playersList.withIndex()) {
                key(player.id) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = HalfHatIcon.DeviceGamePlayerIcon,
                            modifier = Modifier.size(24.dp),
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            label = { Text(text = "Name") },
                            value = player.name,
                            onValueChange = { component.onChangePLayersName(index, it) },
                            isError = player in showErrorForPlayers,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(
                            enabled = playersList.size > 2u,
                            onClick = { component.onRemovePLayer(index) },
                        ) {
                            Icon(
                                imageVector = HalfHatIcon.DeviceGameRemovePlayer,
                                modifier = Modifier.size(24.dp),
                                contentDescription = null,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = component.onAddPLayer,
            ) {
                Icon(
                    imageVector = HalfHatIcon.DeviceGameAddPlayerButton,
                    modifier = Modifier.size(24.dp),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add new player",
                )
            }
            Spacer(modifier = Modifier.height(72.dp).background(Color.Yellow))
        }
        BottomAppBar(
            actions = {
                IconButton(
                    onClick = component.onShufflePlayersList
                ) {
                    Icon(
                        imageVector = HalfHatIcon.DeviceGameShufflePlayersButton,
                        modifier = Modifier.size(24.dp),
                        contentDescription = "Shuffle players",
                    )
                }
                IconButton(
                    onClick = component.onOpenGameSettings
                ) {
                    Icon(
                        imageVector = HalfHatIcon.DeviceGameSettingsButton,
                        modifier = Modifier.size(24.dp),
                        contentDescription = "Game settings",
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = component.onStartGame,
                ) {
                    Icon(
                        imageVector = HalfHatIcon.DeviceGameStartGameButton,
                        modifier = Modifier.size(24.dp),
                        contentDescription = "Start game",
                    )
                }
            }
        )
    }
}