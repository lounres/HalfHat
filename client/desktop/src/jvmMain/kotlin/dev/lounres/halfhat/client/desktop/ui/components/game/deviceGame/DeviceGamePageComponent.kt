package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomScreen.RoomScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomSettings.RoomSettingsComponent


interface DeviceGamePageComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class RoomScreen(val component: RoomScreenComponent) : Child
        data class RoomSettings(val component: RoomSettingsComponent) : Child
        data class GameScreen(val component: GameScreenComponent) : Child
    }
}