package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.previewScreen

import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


public interface PreviewScreenComponent {
    public val currentRoomSearchEntry: StateFlow<String>
    public val onChangeRoomSearchEntry: (String) -> Unit
    public val generateRoomSearchEntry: () -> Unit
    
    public val currentRoomPreview: StateFlow<RoomPreview>
    public val currentEnterName: MutableStateFlow<String>
    public val onJoinRoom: () -> Unit
    
    public sealed interface RoomPreview {
        public data object Empty: RoomPreview
        public data object Loading: RoomPreview
        public data class Present(val info: ServerApi.RoomDescription): RoomPreview
    }
}