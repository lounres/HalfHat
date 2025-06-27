package dev.lounres.halfhat.client.common.ui.components.game

import dev.lounres.halfhat.client.common.ui.components.PageComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.DeviceGamePageComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.OnlineGamePageComponent
import dev.lounres.halfhat.client.common.ui.components.game.timer.TimerPageComponent
import dev.lounres.halfhat.client.common.ui.components.game.localGame.LocalGamePageComponent
import dev.lounres.halfhat.client.common.ui.components.game.modeSelection.ModeSelectionPageComponent
import dev.lounres.komponentual.navigation.ChildrenSlot
import dev.lounres.kone.state.KoneState


public interface GamePageComponent : PageComponent {
    override val textName: String get() = "Game"
    public val currentChild: KoneState<ChildrenSlot<*, Child>>
    
    public sealed interface Child {
        public class ModeSelection(public val component: ModeSelectionPageComponent) : Child
        public class OnlineGame(public val component: OnlineGamePageComponent) : Child
        public class LocalGame(public val component: LocalGamePageComponent) : Child
        public class DeviceGame(public val component: DeviceGamePageComponent) : Child
        public class GameTimer(public val component: TimerPageComponent) : Child
    }
}