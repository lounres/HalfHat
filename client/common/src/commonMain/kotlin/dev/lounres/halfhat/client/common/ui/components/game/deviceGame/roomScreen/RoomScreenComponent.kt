package dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.flow.StateFlow


// TODO: Thin about replacing the class with data class with temporary ID
public class Player(name: String) {
    public var name: String by mutableStateOf(name)
}

public interface RoomScreenComponent {
    public val onExitDeviceGame: () -> Unit
    
    public val playersList: StateFlow<KoneList<Player>>
    public val showErrorForEmptyPlayerNames: StateFlow<Boolean>
    public val onChangePLayersName: (index: UInt, newName: String) -> Unit
    public val onRemovePLayer: (index: UInt) -> Unit
    public val onAddPLayer: () -> Unit
    public val onShufflePlayersList: () -> Unit

    public val onOpenGameSettings: () -> Unit
    public val onStartGame: () -> Unit
}