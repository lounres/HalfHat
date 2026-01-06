package dev.lounres.halfhat.client.ui.components.game.deviceGame

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.ui.components.game.deviceGame.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.ui.components.game.deviceGame.roomScreen.RoomScreenComponent
import dev.lounres.halfhat.client.ui.components.game.deviceGame.roomSettings.RoomSettingsComponent
import dev.lounres.halfhat.client.components.navigation.ChildrenStack
import dev.lounres.kone.hub.KoneAsynchronousHubView


public interface DeviceGamePageComponent {
    public val childStack: KoneAsynchronousHubView<ChildrenStack<*, Child, UIComponentContext>, *>

    public sealed interface Child {
        public data class RoomScreen(val component: RoomScreenComponent) : Child
        public data class RoomSettings(val component: RoomSettingsComponent) : Child
        public data class GameScreen(val component: GameScreenComponent) : Child
    }
}