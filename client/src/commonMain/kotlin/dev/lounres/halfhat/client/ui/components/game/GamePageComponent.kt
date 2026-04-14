package dev.lounres.halfhat.client.ui.components.game

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.ui.components.game.controller.ControllerPageComponent
import dev.lounres.halfhat.client.ui.components.game.deviceGame.DeviceGamePageComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.OnlineGamePageComponent
import dev.lounres.halfhat.client.ui.components.game.timer.TimerPageComponent
import dev.lounres.halfhat.client.ui.components.game.localGame.LocalGamePageComponent
import dev.lounres.halfhat.client.ui.components.game.modeSelection.ModeSelectionPageComponent
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.kone.hub.KoneAsynchronousHub


public interface GamePageComponent {
    public val currentChild: KoneAsynchronousHub<ChildrenSlot<*, Child, UIComponentContext>>
    
    public sealed interface Child {
        public class ModeSelection(public val component: ModeSelectionPageComponent) : Child
        public class OnlineGame(public val component: OnlineGamePageComponent) : Child
        public class LocalGame(public val component: LocalGamePageComponent) : Child
        public class DeviceGame(public val component: DeviceGamePageComponent) : Child
        public class GameController(public val component: ControllerPageComponent) : Child
        public class GameTimer(public val component: TimerPageComponent) : Child
    }
}