package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.previewScreen.FakePreviewScreenComponent


class FakeOnlineGamePageComponent(
    initialChild: OnlineGamePageComponent.Child = OnlineGamePageComponent.Child.PreviewScreen(FakePreviewScreenComponent())
) : OnlineGamePageComponent {
    override val childStack: Value<ChildStack<*, OnlineGamePageComponent.Child>> = MutableValue(ChildStack(Unit, initialChild))
}