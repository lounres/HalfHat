package dev.lounres.halfhat.client.desktop.ui.components.game.modeSelection


interface ModeSelectionPageComponent {
    val onOnlineGameSelect: () -> Unit
    val onLocalGameSelect: () -> Unit
    val onDeviceGameSelect: () -> Unit
    val onGameTimerSelect: () -> Unit
    
    val onOnlineGameInfo: () -> Unit
    val onLocalGameInfo: () -> Unit
    val onDeviceGameInfo: () -> Unit
    val onGameTimerInfo: () -> Unit
}