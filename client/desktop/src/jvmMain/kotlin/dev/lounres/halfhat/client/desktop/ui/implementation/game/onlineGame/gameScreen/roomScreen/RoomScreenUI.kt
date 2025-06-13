package dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roomScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.lounres.halfhat.client.desktop.resources.*
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomScreen.RoomScreenComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.utils.withIndex
import org.jetbrains.compose.resources.painterResource
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes


@Composable
fun RowScope.RoomScreenActionsUI(
    component: RoomScreenComponent,
) {

}

@Composable
fun ColumnScope.RoomScreenUI(
    component: RoomScreenComponent,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val gameState = component.gameStateFlow.collectAsState().value
        val playersList = gameState.playersList
        val userIndex = gameState.role.userIndex
        Spacer(modifier = Modifier.height(16.dp))
        for ((index, player) in playersList.withIndex()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (index == 0u)
                    Icon(
                        painter = painterResource(DesktopRes.drawable.onlineGameHostMark_dark_png_24dp),
                        modifier = Modifier.size(24.dp),
                        contentDescription = null,
                    )
                else
                    Spacer(Modifier.width(24.dp))
                Spacer(Modifier.width(4.dp))
                Icon(
                    painter = painterResource(DesktopRes.drawable.deviceGamePlayerIcon_dark_png_24dp),
                    modifier = Modifier.size(24.dp),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(16.dp))
                if (index == userIndex) Text(text = "${player.name} (you)", fontWeight = FontWeight.Bold)
                else Text(text = player.name)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.height(72.dp).background(Color.Yellow))
    }
}

@Composable
fun RowScope.RoomScreenButtonsUI(
    component: RoomScreenComponent,
) {
    IconButton(
        onClick = component.onOpenGameSettings
    ) {
        Icon(
            painter = painterResource(DesktopRes.drawable.gameSettingsaButton_dark_png_24dp),
            modifier = Modifier.size(24.dp),
            contentDescription = "Game settings",
        )
    }
    Spacer(Modifier.weight(1f))
    if (component.gameStateFlow.collectAsState().value.role.userIndex == 0u)
        FloatingActionButton(
            onClick = component.onStartGame,
            shape = CircleShape,
        ) {
            Icon(
                painter = painterResource(DesktopRes.drawable.startDeviceGameButton_dark_png_24dp),
                modifier = Modifier.size(24.dp),
                contentDescription = "Start game",
            )
        }
}