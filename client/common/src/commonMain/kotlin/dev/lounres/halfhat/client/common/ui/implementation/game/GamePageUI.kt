package dev.lounres.halfhat.client.common.ui.implementation.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.gamePage_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.game.GamePageComponent
import dev.lounres.halfhat.client.common.ui.implementation.game.deviceGame.DeviceGamePageActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.deviceGame.DeviceGamePageUI
import dev.lounres.halfhat.client.common.ui.implementation.game.localGame.LocalGamePageActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.localGame.LocalGamePageUI
import dev.lounres.halfhat.client.common.ui.implementation.game.modeSelection.ModeSelectionPageUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.OnlineGamePageActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.OnlineGamePageUI
import dev.lounres.halfhat.client.common.ui.implementation.game.timer.TimerPageActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.timer.TimerPageUI
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import dev.lounres.kone.hub.subscribeAsState
import org.jetbrains.compose.resources.painterResource


@Composable
public fun GamePageIcon(
    isSelected: Boolean,
) {
    Icon(
        painter = painterResource(Res.drawable.gamePage_dark_png_24dp),
        modifier = commonIconModifier,
        contentDescription = "Game page",
    )
}

@Composable
public fun GamePageBadge(
    component: GamePageComponent,
    isSelected: Boolean,
) {

}

@Composable
public fun RowScope.GamePageActionsUI(
    component: GamePageComponent,
) {
    when (val active = component.currentChild.subscribeAsState().value.component) {
        is GamePageComponent.Child.ModeSelection -> {}
        is GamePageComponent.Child.OnlineGame -> OnlineGamePageActionsUI(active.component)
        is GamePageComponent.Child.LocalGame -> LocalGamePageActionsUI(active.component)
        is GamePageComponent.Child.DeviceGame -> DeviceGamePageActionsUI(active.component)
        is GamePageComponent.Child.GameTimer -> TimerPageActionsUI(active.component)
    }
}

@Composable
public fun GamePageUI(
    component: GamePageComponent,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 480.dp)
                .align(Alignment.Center),
        ) {
            when (val active = component.currentChild.subscribeAsState().value.component) {
                is GamePageComponent.Child.ModeSelection -> ModeSelectionPageUI(active.component)
                is GamePageComponent.Child.OnlineGame -> OnlineGamePageUI(active.component)
                is GamePageComponent.Child.LocalGame -> LocalGamePageUI(active.component)
                is GamePageComponent.Child.DeviceGame -> DeviceGamePageUI(active.component)
                is GamePageComponent.Child.GameTimer -> TimerPageUI(active.component)
            }
        }
    }
}