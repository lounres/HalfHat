package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.loading


class FakeLoadingComponent: LoadingComponent {
    override val onCopyOnlineGameKey: () -> Unit = {}
    override val onCopyOnlineGameLink: () -> Unit = {}
    override val onExitOnlineGame: () -> Unit = {}
}