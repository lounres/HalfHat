package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomScreen

import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.flow.StateFlow


interface RoomScreenComponent {
    val onExitDeviceGame: () -> Unit
    
    val playersList: StateFlow<KoneList<String>>
    val showErrorForEmptyPlayerNames: StateFlow<Boolean>
    val onChangePLayersName: (index: UInt, newName: String) -> Unit
    val onRemovePLayer: (index: UInt) -> Unit
    val onAddPLayer: () -> Unit
    val onShufflePlayersList: () -> Unit

    val onOpenGameSettings: () -> Unit
    val onStartGame: () -> Unit
}