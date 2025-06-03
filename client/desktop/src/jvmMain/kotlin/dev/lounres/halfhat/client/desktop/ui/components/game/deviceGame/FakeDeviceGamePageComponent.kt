package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame

import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.FakeGameScreenComponent
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.kone.state.KoneMutableState
import dev.lounres.kone.state.KoneState


class FakeDeviceGamePageComponent(
    child: DeviceGamePageComponent.Child = DeviceGamePageComponent.Child.GameScreen(FakeGameScreenComponent()),
): DeviceGamePageComponent {
    override val childStack: KoneState<ChildrenStack<*, DeviceGamePageComponent.Child>> =
        KoneMutableState(ChildrenStack("", child))
}