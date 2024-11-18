package dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.OnlineGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.GameScreenActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.GameScreenUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.previewScreen.PreviewScreenActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.previewScreen.PreviewScreenUI


@Composable
fun RowScope.OnlineGamePageActionsUI(
    component: OnlineGamePageComponent,
) {
    when (val child = component.childStack.subscribeAsState().value.active.instance) {
        is OnlineGamePageComponent.Child.PreviewScreen -> PreviewScreenActionsUI(child.component)
        is OnlineGamePageComponent.Child.GameScreen -> GameScreenActionsUI(child.component)
    }
}

@Composable
fun OnlineGamePageUI(
    component: OnlineGamePageComponent,
) {
    when (val child = component.childStack.subscribeAsState().value.active.instance) {
        is OnlineGamePageComponent.Child.PreviewScreen -> PreviewScreenUI(child.component)
        is OnlineGamePageComponent.Child.GameScreen -> GameScreenUI(child.component)
    }
}