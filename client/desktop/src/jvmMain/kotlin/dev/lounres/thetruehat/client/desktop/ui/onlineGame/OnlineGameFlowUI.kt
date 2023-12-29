package dev.lounres.thetruehat.client.desktop.ui.onlineGame

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.lounres.thetruehat.client.common.components.onlineGame.OnlineGameFlowComponent
import dev.lounres.thetruehat.client.desktop.ui.onlineGame.gameResults.GameResultsPageUI
import dev.lounres.thetruehat.client.desktop.ui.onlineGame.roomEnter.RoomEnterPageUI
import dev.lounres.thetruehat.client.desktop.ui.onlineGame.roomFlow.RoomFlowUI
import dev.lounres.thetruehat.client.desktop.ui.onlineGame.roundBreak.RoundBreakPageUI
import dev.lounres.thetruehat.client.desktop.ui.onlineGame.roundCountdown.RoundCountdownPageUI
import dev.lounres.thetruehat.client.desktop.ui.onlineGame.roundEditing.RoundEditingPageUI
import dev.lounres.thetruehat.client.desktop.ui.onlineGame.roundInProgress.RoundInProgressPageUI


@Preview
@Composable
public fun OnlineGameFlowUIPreview() {
    OnlineGameFlowUI(
        component = TODO()
    )
}

@Composable
public fun OnlineGameFlowUI(
    component: OnlineGameFlowComponent,
) {
    val slot by component.childSlot.subscribeAsState()
    when(val child = slot.child!!.instance) {
        is OnlineGameFlowComponent.Child.RoomEnter -> RoomEnterPageUI(child.component)
        is OnlineGameFlowComponent.Child.RoomFlow -> RoomFlowUI(child.component)
        is OnlineGameFlowComponent.Child.RoundBreak -> RoundBreakPageUI(child.component)
        is OnlineGameFlowComponent.Child.RoundCountdown -> RoundCountdownPageUI(child.component)
        is OnlineGameFlowComponent.Child.RoundInProgress -> RoundInProgressPageUI(child.component)
        is OnlineGameFlowComponent.Child.RoundEditing -> RoundEditingPageUI(child.component)
        is OnlineGameFlowComponent.Child.GameResults -> GameResultsPageUI(child.component)
    }
}