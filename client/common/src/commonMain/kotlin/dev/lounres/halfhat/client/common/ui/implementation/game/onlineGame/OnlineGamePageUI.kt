package dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.common.logic.components.game.onlineGame.ConnectionStatus
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.exitDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.common.resources.gameSettingsaButton_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.OnlineGamePageComponent
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.GameScreenActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.GameScreenUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.previewScreen.PreviewScreenActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.previewScreen.PreviewScreenUI
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import dev.lounres.kone.state.subscribeAsState
import org.jetbrains.compose.resources.painterResource


@Composable
public fun RowScope.OnlineGamePageActionsUI(
    component: OnlineGamePageComponent,
) {
    when (val child = component.childStack.subscribeAsState().value.active.component) {
        is OnlineGamePageComponent.Child.PreviewScreen -> PreviewScreenActionsUI(child.component)
        is OnlineGamePageComponent.Child.GameScreen -> GameScreenActionsUI(child.component)
    }
    IconButton(
        onClick = component.onExitOnlineGameMode
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(Res.drawable.exitDeviceGameButton_dark_png_24dp), // TODO: Copy the icons
            contentDescription = "Exit online game"
        )
    }
}

@Composable
public fun ColumnScope.OnlineGamePageUI(
    component: OnlineGamePageComponent,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, color = Color.Gray)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val connectionStatus by component.connectionStatus.collectAsState()
                    Text(
                        text = when (connectionStatus) {
                            ConnectionStatus.Connected -> "Connected"
                            ConnectionStatus.Disconnected -> "Connecting..."
                        },
                        fontSize = 16.sp,
                    )
                    Canvas(
                        modifier = Modifier.size(8.dp),
                    ) {
                        drawCircle(
                            color = when (connectionStatus) {
                                ConnectionStatus.Connected -> Color.Green
                                ConnectionStatus.Disconnected -> Color.Red
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            OutlinedIconButton(
                onClick = { TODO() },
                border = BorderStroke(1.dp, color = Color.Gray),
            ) {
                Icon(
                    modifier = commonIconModifier,
                    painter = painterResource(Res.drawable.gameSettingsaButton_dark_png_24dp), // TODO: Replace in future
                    contentDescription = "Connection settings",
                )
            }
        }
        when (val child = component.childStack.subscribeAsState().value.active.component) {
            is OnlineGamePageComponent.Child.PreviewScreen -> PreviewScreenUI(child.component)
            is OnlineGamePageComponent.Child.GameScreen -> GameScreenUI(child.component)
        }
    }
}