package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.FakeGameScreenComponent


class FakeDeviceGamePageComponent(
    child: DeviceGamePageComponent.Child = DeviceGamePageComponent.Child.GameScreen(FakeGameScreenComponent()),
): DeviceGamePageComponent {
    override val childStack: Value<ChildStack<*, DeviceGamePageComponent.Child>> =
        MutableValue(ChildStack("", child))
}