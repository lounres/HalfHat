package dev.lounres.halfhat.client.desktop.ui.components.game

import dev.lounres.halfhat.client.desktop.ui.components.PageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.DeviceGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.localGame.LocalGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.modeSelection.ModeSelectionPageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.OnlineGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.timer.TimerPageComponent
import dev.lounres.komponentual.navigation.ChildrenSlot
import dev.lounres.kone.state.KoneState


interface GamePageComponent : PageComponent {
    override val textName: String get() = "Game"
    val currentChild: KoneState<ChildrenSlot<*, Child>>
    
    sealed interface Child {
        class ModeSelection(val component: ModeSelectionPageComponent) : Child
        class OnlineGame(val component: OnlineGamePageComponent) : Child
        class LocalGame(val component: LocalGamePageComponent) : Child
        class DeviceGame(val component: DeviceGamePageComponent) : Child
        class GameTimer(val component: TimerPageComponent) : Child
    }
}