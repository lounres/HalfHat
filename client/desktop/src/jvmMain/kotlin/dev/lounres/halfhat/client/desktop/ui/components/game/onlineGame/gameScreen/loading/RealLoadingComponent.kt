package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.loading


class RealLoadingComponent(
    override val onCopyOnlineGameKey: () -> Unit,
    override val onCopyOnlineGameLink: () -> Unit,
    override val onExitOnlineGame: () -> Unit,
): LoadingComponent