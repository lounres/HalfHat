package dev.lounres.halfhat.client.desktop.ui.components.game.modeSelection


class RealModeSelectionPageComponent(
    override val onOnlineGameSelect: () -> Unit,
    override val onLocalGameSelect: () -> Unit,
    override val onDeviceGameSelect: () -> Unit,
    override val onGameTimerSelect: () -> Unit,
    
    override val onOnlineGameInfo: () -> Unit,
    override val onLocalGameInfo: () -> Unit,
    override val onDeviceGameInfo: () -> Unit,
    override val onGameTimerInfo: () -> Unit,
): ModeSelectionPageComponent