package dev.lounres.halfhat.client.ui.components.game.onlineGame.previewScreen

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.logic.components.game.onlineGame.ConnectionStatus
import dev.lounres.kone.hub.KoneAsynchronousHub
import kotlinx.coroutines.flow.StateFlow


public interface PreviewScreenComponent {
    public val onExitOnlineGameMode: () -> Unit
    
    public val connectionStatus: StateFlow<ConnectionStatus>
    
    public val currentRoomSearchEntry: KoneAsynchronousHub<String>
    public val onChangeRoomSearchEntry: (String) -> Unit
    public val generateRoomSearchEntry: () -> Unit
    
    public val currentRoomPreview: StateFlow<RoomPreview>
    public val currentEnterName: KoneAsynchronousHub<String>
    public val onSetEnterName: (String) -> Unit
    public val onJoinRoom: () -> Unit
    
    public sealed interface RoomPreview {
        public data object Empty: RoomPreview
        public data object Loading: RoomPreview
        public data class Present(val info: ServerApi.RoomDescription): RoomPreview
    }
}