package dev.lounres.thetruehat.client.desktop.ui.game

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.lounres.thetruehat.client.common.components.game.GameFlowComponent
import dev.lounres.thetruehat.client.desktop.ui.game.gameResults.GameResultsPageUI
import dev.lounres.thetruehat.client.desktop.ui.game.roomEnter.RoomEnterPageUI
import dev.lounres.thetruehat.client.desktop.ui.game.roomFlow.RoomFlowUI
import dev.lounres.thetruehat.client.desktop.ui.game.roundBreak.RoundBreakPageUI
import dev.lounres.thetruehat.client.desktop.ui.game.roundEditing.RoundEditingPageUI
import dev.lounres.thetruehat.client.desktop.ui.game.roundInProgress.RoundInProgressPageUI


@Preview
@Composable
fun GameFlowUIPreview() {
    GameFlowUI(
        component = TODO()
    )
}

@Composable
fun GameFlowUI(
    component: GameFlowComponent,
) {
    val slot by component.childSlot.subscribeAsState()
    when(val child = slot.child!!.instance) {
        is GameFlowComponent.Child.RoomEnter -> RoomEnterPageUI(child.component)
        is GameFlowComponent.Child.RoomFlow -> RoomFlowUI(child.component)
        is GameFlowComponent.Child.RoundBreak -> RoundBreakPageUI(child.component)
        is GameFlowComponent.Child.RoundInProgress -> RoundInProgressPageUI(child.component)
        is GameFlowComponent.Child.RoundEditing -> RoundEditingPageUI(child.component)
        is GameFlowComponent.Child.GameResults -> GameResultsPageUI(child.component)
    }
}