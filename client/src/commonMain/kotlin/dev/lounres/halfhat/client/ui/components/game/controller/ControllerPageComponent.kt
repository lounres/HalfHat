package dev.lounres.halfhat.client.ui.components.game.controller

import dev.lounres.halfhat.client.ui.components.game.controller.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.ui.components.game.controller.roomScreen.RoomScreenComponent
import dev.lounres.halfhat.client.ui.components.game.controller.roomSettings.RoomSettingsComponent
import dev.lounres.halfhat.client.components.navigation.ChildrenStack
import dev.lounres.kone.hub.KoneAsynchronousHub


public interface ControllerPageComponent {
    public val childStack: KoneAsynchronousHub<ChildrenStack<*, Child>>
    
    public sealed interface Child {
        public data class RoomScreen(val component: RoomScreenComponent) : Child
        public data class RoomSettings(val component: RoomSettingsComponent) : Child
        public data class GameScreen(val component: GameScreenComponent) : Child
    }
}