package dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roundWaiting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import dev.lounres.halfhat.client.desktop.resources.deviceGameListenerIcon_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.deviceGameSpeakerIcon_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.exitDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.finishDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.onlineGameKey_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.onlineGameLink_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundWaiting.RoundWaitingComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
fun RowScope.RoundWaitingActionsUI(
    component: RoundWaitingComponent,
) {
    IconButton(
        onClick = component.onFinishGame
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.finishDeviceGameButton_dark_png_24dp), // TODO: Copy the icons
            contentDescription = "Finish online game"
        )
    }
    IconButton(
        onClick = component.onCopyOnlineGameKey
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.onlineGameKey_dark_png_24dp),
            contentDescription = "Copy online game key"
        )
    }
    IconButton(
        onClick = component.onCopyOnlineGameLink
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.onlineGameLink_dark_png_24dp),
            contentDescription = "Copy online game link"
        )
    }
    IconButton(
        onClick = component.onExitOnlineGame
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.exitDeviceGameButton_dark_png_24dp), // TODO: Copy the icons
            contentDescription = "Exit online game"
        )
    }
}

@Composable
fun RoundWaitingUI(
    component: RoundWaitingComponent,
) {
    val gameState by component.gameState.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Icon(
                    painter = painterResource(DesktopRes.drawable.deviceGameSpeakerIcon_dark_png_24dp),
                    modifier = Modifier.size(24.dp),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${gameState.playersList[gameState.speakerIndex]} explains",
                    fontSize = 24.sp,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "${gameState.playersList[gameState.listenerIndex]} guesses",
                    fontSize = 24.sp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(DesktopRes.drawable.deviceGameListenerIcon_dark_png_24dp),
                    modifier = Modifier.size(24.dp),
                    contentDescription = null
                )
            }
        }
        when {
            gameState.userIndex == gameState.speakerIndex -> {
                Spacer(modifier = Modifier.height(8.dp))
                if (!gameState.speakerReady)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        onClick = component.onSpeakerReady,
                    ) {
                        Text(
                            text = "I am ready to explain",
                            fontSize = 32.sp,
                        )
                    }
                else
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        shape = CircleShape,
                        onClick = component.onSpeakerReady,
                    ) {
                        Text(
                            text = "Wait for your partner",
                            fontSize = 32.sp,
                        )
                    }
            }
            gameState.userIndex == gameState.listenerIndex -> {
                Spacer(modifier = Modifier.height(8.dp))
                if (!gameState.listenerReady)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        onClick = component.onListenerReady,
                    ) {
                        Text(
                            text = "I am ready to guess",
                            fontSize = 32.sp,
                        )
                    }
                else
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        shape = CircleShape,
                        onClick = component.onListenerReady,
                    ) {
                        Text(
                            text = "Wait for your partner",
                            fontSize = 32.sp,
                        )
                    }
            }
            else -> {}
        }
    }
}