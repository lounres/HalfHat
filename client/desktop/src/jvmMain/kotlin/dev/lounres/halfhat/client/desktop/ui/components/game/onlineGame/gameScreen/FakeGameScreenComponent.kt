package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen

import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomScreen.FakeRoomScreenComponent
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.kone.state.KoneMutableState
import dev.lounres.kone.state.KoneState


class FakeGameScreenComponent(
    initialChild: GameScreenComponent.Child = GameScreenComponent.Child.RoomScreen(FakeRoomScreenComponent())
) : GameScreenComponent {
    override val onCopyOnlineGameKey: () -> Unit = {}
    override val onCopyOnlineGameLink: () -> Unit = {}
    override val onExitOnlineGame: () -> Unit = {}
    
    override val childStack: KoneState<ChildrenStack<*, GameScreenComponent.Child>> =
        KoneMutableState(ChildrenStack(Unit, initialChild))
}