package dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomScreen

import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.set.KoneSet
import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.Uuid


// TODO: Thin about replacing the class with data class with temporary ID
public data class Player(
    public val name: String,
    public val id: Uuid = Uuid.random(),
)

public interface RoomScreenComponent {
    public val onExitDeviceGame: () -> Unit
    
    public val playersList: StateFlow<KoneList<Player>>
    public val showErrorForPlayers: StateFlow<KoneSet<Player>>
    public val onChangePLayersName: (index: UInt, newName: String) -> Unit
    public val onRemovePLayer: (index: UInt) -> Unit
    public val onAddPLayer: () -> Unit
    public val onShufflePlayersList: () -> Unit

    public val onOpenGameSettings: () -> Unit
    public val onStartGame: () -> Unit
}