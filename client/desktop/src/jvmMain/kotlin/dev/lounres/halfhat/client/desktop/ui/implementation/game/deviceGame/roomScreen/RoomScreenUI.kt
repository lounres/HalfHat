package dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.roomScreen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.lounres.halfhat.client.desktop.resources.addPlayerButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.deviceGamePlayerIcon_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.exitDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.gameSettingsaButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.removePlayerButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.shufflePlayersButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.startDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomScreen.RoomScreenComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.utils.withIndex
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import org.jetbrains.compose.resources.painterResource


@Composable
fun RowScope.RoomScreenActionsUI(
    component: RoomScreenComponent,
) {
    IconButton(
        onClick = component.onExitDeviceGame
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.exitDeviceGameButton_dark_png_24dp),
            contentDescription = "Exit device game"
        )
    }
}

@Composable
fun RoomScreenUI(
    component: RoomScreenComponent,
) {
    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val playersList = component.playersList.collectAsState().value
        val showErrorForEmptyPlayerNames = component.showErrorForEmptyPlayerNames.collectAsState().value
        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 480.dp)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            for ((index, playerName) in playersList.withIndex()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(DesktopRes.drawable.deviceGamePlayerIcon_dark_png_24dp),
                        modifier = Modifier.size(24.dp),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TextField(
                        modifier = Modifier.weight(1f),
                        label = { Text(text = "Name") },
                        value = playerName,
                        onValueChange = { component.onChangePLayersName(index, it) },
                        isError = showErrorForEmptyPlayerNames && playerName.isBlank(),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = { component.onRemovePLayer(index) }
                    ) {
                        Icon(
                            painter = painterResource(DesktopRes.drawable.removePlayerButton_dark_png_24dp),
                            modifier = Modifier.size(24.dp),
                            contentDescription = null,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = component.onAddPLayer,
            ) {
                Icon(
                    painter = painterResource(DesktopRes.drawable.addPlayerButton_dark_png_24dp),
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
                        painter = painterResource(DesktopRes.drawable.shufflePlayersButton_dark_png_24dp),
                        modifier = Modifier.size(24.dp),
                        contentDescription = "Shuffle players",
                    )
                }
                IconButton(
                    onClick = component.onOpenGameSettings
                ) {
                    Icon(
                        painter = painterResource(DesktopRes.drawable.gameSettingsaButton_dark_png_24dp),
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
                        painter = painterResource(DesktopRes.drawable.startDeviceGameButton_dark_png_24dp),
                        modifier = Modifier.size(24.dp),
                        contentDescription = "Start game",
                    )
                }
            }
        )
    }
}