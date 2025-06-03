package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame

import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.previewScreen.FakePreviewScreenComponent
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.kone.state.KoneMutableState
import dev.lounres.kone.state.KoneState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeOnlineGamePageComponent(
    initialChild: OnlineGamePageComponent.Child = OnlineGamePageComponent.Child.PreviewScreen(FakePreviewScreenComponent()),
    initialConnectionStatus: ConnectionStatus = ConnectionStatus.Connected
) : OnlineGamePageComponent {
    override val childStack: KoneState<ChildrenStack<*, OnlineGamePageComponent.Child>> =
        KoneMutableState(ChildrenStack(Unit, initialChild))
    override val connectionStatus: StateFlow<ConnectionStatus> = MutableStateFlow(initialConnectionStatus)
}