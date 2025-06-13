package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.flow.StateFlow


// TODO: Thin about replacing the class with data class with temporary ID
class Player(name: String) {
    var name by mutableStateOf(name)
}

interface RoomScreenComponent {
    val onExitDeviceGame: () -> Unit
    
    val playersList: StateFlow<KoneList<Player>>
    val showErrorForEmptyPlayerNames: StateFlow<Boolean>
    val onChangePLayersName: (index: UInt, newName: String) -> Unit
    val onRemovePLayer: (index: UInt) -> Unit
    val onAddPLayer: () -> Unit
    val onShufflePlayersList: () -> Unit

    val onOpenGameSettings: () -> Unit
    val onStartGame: () -> Unit
}