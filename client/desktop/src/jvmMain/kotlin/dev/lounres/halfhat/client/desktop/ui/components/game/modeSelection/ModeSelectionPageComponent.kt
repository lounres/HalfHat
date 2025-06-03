package dev.lounres.halfhat.client.desktop.ui.components.game.modeSelection

import dev.lounres.komponentual.navigation.ChildrenPossibility
import dev.lounres.kone.state.KoneState


interface ModeSelectionPageComponent {
    val onOnlineGameSelect: () -> Unit
    val onLocalGameSelect: () -> Unit
    val onDeviceGameSelect: () -> Unit
    val onGameTimerSelect: () -> Unit
    
    val infoPopup: KoneState<ChildrenPossibility<*, InfoPopup>>
    
    val onOnlineGameInfo: () -> Unit
    val onLocalGameInfo: () -> Unit
    val onDeviceGameInfo: () -> Unit
    val onGameTimerInfo: () -> Unit
    val onCloseInfo: () -> Unit
    
    enum class InfoPopup {
        OnlineGame, LocalGame, DeviceGame, GameTimer
    }
}