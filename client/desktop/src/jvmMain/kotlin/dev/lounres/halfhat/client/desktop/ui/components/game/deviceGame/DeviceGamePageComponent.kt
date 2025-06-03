package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame

import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomScreen.RoomScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomSettings.RoomSettingsComponent
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.kone.state.KoneState


interface DeviceGamePageComponent {
    val childStack: KoneState<ChildrenStack<*, Child>>

    sealed interface Child {
        data class RoomScreen(val component: RoomScreenComponent) : Child
        data class RoomSettings(val component: RoomSettingsComponent) : Child
        data class GameScreen(val component: GameScreenComponent) : Child
    }
}