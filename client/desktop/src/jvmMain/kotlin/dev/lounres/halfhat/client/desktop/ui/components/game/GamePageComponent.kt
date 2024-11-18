package dev.lounres.halfhat.client.desktop.ui.components.game

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.DeviceGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.localGame.LocalGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.modeSelection.ModeSelectionPageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.OnlineGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.timer.TimerPageComponent


interface GamePageComponent {
    val childStack: Value<ChildStack<*, Child>>
    
    sealed interface Child {
        class ModeSelection(val component: ModeSelectionPageComponent) : Child
        class OnlineGame(val component: OnlineGamePageComponent) : Child
        class LocalGame(val component: LocalGamePageComponent) : Child
        class DeviceGame(val component: DeviceGamePageComponent) : Child
        class GameTimer(val component: TimerPageComponent) : Child
    }
}