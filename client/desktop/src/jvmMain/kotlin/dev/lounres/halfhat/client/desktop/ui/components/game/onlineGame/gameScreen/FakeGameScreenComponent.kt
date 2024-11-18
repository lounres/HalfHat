package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomScreen.FakeRoomScreenComponent


class FakeGameScreenComponent(
    initialChild: GameScreenComponent.Child = GameScreenComponent.Child.RoomScreen(FakeRoomScreenComponent())
) : GameScreenComponent {
    override val childStack: Value<ChildStack<*, GameScreenComponent.Child>> = MutableValue(ChildStack(Unit, initialChild))
}