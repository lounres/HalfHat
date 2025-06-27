package dev.lounres.halfhat.client.common.ui.components.game.deviceGame

import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomScreen.RoomScreenComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomSettings.RoomSettingsComponent
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.kone.state.KoneState


public interface DeviceGamePageComponent {
    public val childStack: KoneState<ChildrenStack<*, Child>>

    public sealed interface Child {
        public data class RoomScreen(val component: RoomScreenComponent) : Child
        public data class RoomSettings(val component: RoomSettingsComponent) : Child
        public data class GameScreen(val component: GameScreenComponent) : Child
    }
}