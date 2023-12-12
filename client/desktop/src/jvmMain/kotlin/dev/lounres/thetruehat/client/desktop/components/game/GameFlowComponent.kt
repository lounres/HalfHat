package dev.lounres.thetruehat.client.desktop.components.game

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.desktop.components.game.gameResults.GameResultsPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.roomEnter.RoomEnterPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.roomFlow.RoomFlowComponent
import dev.lounres.thetruehat.client.desktop.components.game.roundBreak.RoundBreakPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.roundEditing.RoundEditingPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.roundInProgress.RoundInProgressPageComponent


interface GameFlowComponent {
    val childSlot: Value<ChildSlot<*, Child>>

    sealed interface Child {
        data class RoomEnter(val component: RoomEnterPageComponent) : Child
        data class RoomFlow(val component: RoomFlowComponent): Child
        data class RoundBreak(val component: RoundBreakPageComponent): Child
        data class RoundInProgress(val component: RoundInProgressPageComponent): Child
        data class RoundEditing(val component: RoundEditingPageComponent): Child
        data class GameResults(val component: GameResultsPageComponent): Child
    }
}