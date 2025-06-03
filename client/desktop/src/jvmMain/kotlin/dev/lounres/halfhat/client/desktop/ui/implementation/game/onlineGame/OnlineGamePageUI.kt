package dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.ConnectionStatus
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.OnlineGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.GameScreenActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.GameScreenUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.previewScreen.PreviewScreenActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.previewScreen.PreviewScreenUI
import dev.lounres.kone.state.subscribeAsState


@Composable
fun RowScope.OnlineGamePageActionsUI(
    component: OnlineGamePageComponent,
) {
    when (val child = component.childStack.subscribeAsState().value.active.component) {
        is OnlineGamePageComponent.Child.PreviewScreen -> PreviewScreenActionsUI(child.component)
        is OnlineGamePageComponent.Child.GameScreen -> GameScreenActionsUI(child.component)
    }
}

@Composable
fun ColumnScope.OnlineGamePageUI(
    component: OnlineGamePageComponent,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp,
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
        when (val child = component.childStack.subscribeAsState().value.active.component) {
            is OnlineGamePageComponent.Child.PreviewScreen -> PreviewScreenUI(child.component)
            is OnlineGamePageComponent.Child.GameScreen -> GameScreenUI(child.component)
        }
    }
}