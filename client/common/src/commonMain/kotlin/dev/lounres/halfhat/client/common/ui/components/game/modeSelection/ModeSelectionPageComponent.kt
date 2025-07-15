package dev.lounres.halfhat.client.common.ui.components.game.modeSelection

import dev.lounres.halfhat.client.components.navigation.ChildrenPossibility
import dev.lounres.kone.hub.KoneAsynchronousHub


public interface ModeSelectionPageComponent {
    public val onOnlineGameSelect: () -> Unit
    public val onLocalGameSelect: () -> Unit
    public val onDeviceGameSelect: () -> Unit
    public val onGameTimerSelect: () -> Unit
    
    public val infoPopup: KoneAsynchronousHub<ChildrenPossibility<*, InfoPopup>>
    
    public val onOnlineGameInfo: () -> Unit
    public val onLocalGameInfo: () -> Unit
    public val onDeviceGameInfo: () -> Unit
    public val onGameTimerInfo: () -> Unit
    public val onCloseInfo: () -> Unit
    
    public enum class InfoPopup {
        OnlineGame, LocalGame, DeviceGame, GameTimer
    }
}