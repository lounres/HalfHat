package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.previewScreen

import dev.lounres.halfhat.api.server.ServerApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


interface PreviewScreenComponent {
    val currentRoomSearchEntry: StateFlow<String>
    val onChangeRoomSearchEntry: (String) -> Unit
    val generateRoomSearchEntry: () -> Unit
    
    val currentRoomPreview: StateFlow<RoomPreview>
    val currentEnterName: MutableStateFlow<String>
    val onJoinRoom: () -> Unit
    
    sealed interface RoomPreview {
        data object Empty: RoomPreview
        data object Loading: RoomPreview
        data class Present(val info: ServerApi.RoomDescription): RoomPreview
    }
}