package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.loading


interface LoadingComponent {
    val onCopyOnlineGameKey: () -> Unit
    val onCopyOnlineGameLink: () -> Unit
    val onExitOnlineGame: () -> Unit
}