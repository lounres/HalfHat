package dev.lounres.thetruehat.client.common.components.game

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.common.components.game.gameResults.GameResultsPageComponent
import dev.lounres.thetruehat.client.common.components.game.roomEnter.RoomEnterPageComponent
import dev.lounres.thetruehat.client.common.components.game.roomFlow.RoomFlowComponent
import dev.lounres.thetruehat.client.common.components.game.roundBreak.RoundBreakPageComponent
import dev.lounres.thetruehat.client.common.components.game.roundCountdown.RoundCountdownPageComponent
import dev.lounres.thetruehat.client.common.components.game.roundEditing.RoundEditingPageComponent
import dev.lounres.thetruehat.client.common.components.game.roundInProgress.RoundInProgressPageComponent


public interface GameFlowComponent {
    public val childSlot: Value<ChildSlot<*, Child>>

    public sealed interface Child {
        public data class RoomEnter(val component: RoomEnterPageComponent) : Child
        public data class RoomFlow(val component: RoomFlowComponent): Child
        public data class RoundBreak(val component: RoundBreakPageComponent): Child
        public data class RoundCountdown(val component: RoundCountdownPageComponent): Child
        public data class RoundInProgress(val component: RoundInProgressPageComponent): Child
        public data class RoundEditing(val component: RoundEditingPageComponent): Child
        public data class GameResults(val component: GameResultsPageComponent): Child
    }
}