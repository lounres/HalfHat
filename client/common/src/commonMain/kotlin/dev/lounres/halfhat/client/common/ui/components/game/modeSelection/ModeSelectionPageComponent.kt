package dev.lounres.halfhat.client.common.ui.components.game.modeSelection

import dev.lounres.komponentual.navigation.ChildrenPossibility
import dev.lounres.kone.state.KoneAsynchronousState
import dev.lounres.kone.state.KoneState


public interface ModeSelectionPageComponent {
    public val onOnlineGameSelect: () -> Unit
    public val onLocalGameSelect: () -> Unit
    public val onDeviceGameSelect: () -> Unit
    public val onGameTimerSelect: () -> Unit
    
    public val infoPopup: KoneAsynchronousState<ChildrenPossibility<*, InfoPopup>>
    
    public val onOnlineGameInfo: () -> Unit
    public val onLocalGameInfo: () -> Unit
    public val onDeviceGameInfo: () -> Unit
    public val onGameTimerInfo: () -> Unit
    public val onCloseInfo: () -> Unit
    
    public enum class InfoPopup {
        OnlineGame, LocalGame, DeviceGame, GameTimer
    }
}