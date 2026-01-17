package dev.lounres.halfhat.client.ui.implementation.game.onlineGame

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
import androidx.window.core.layout.WindowSizeClass
import dev.lounres.halfhat.client.logic.components.game.onlineGame.ConnectionStatus
import dev.lounres.halfhat.client.ui.components.game.onlineGame.OnlineGamePageComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameConnectionSettingsButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameExitModeButton
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.GameScreenUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.previewScreen.PreviewScreenUI
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.kone.hub.subscribeAsState


@Composable
public fun OnlineGamePageUI(
    component: OnlineGamePageComponent,
    windowSizeClass: WindowSizeClass,
) {
    when (val child = component.childSlot.subscribeAsState().value.component) {
        is OnlineGamePageComponent.Child.PreviewScreen -> PreviewScreenUI(child.component)
        is OnlineGamePageComponent.Child.GameScreen -> GameScreenUI(child.component, windowSizeClass)
    }
}