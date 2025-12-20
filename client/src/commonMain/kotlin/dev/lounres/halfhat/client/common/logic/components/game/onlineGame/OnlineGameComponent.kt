package dev.lounres.halfhat.client.common.logic.components.game.onlineGame

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.api.onlineGame.ServerApi
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow


public enum class ConnectionStatus {
    Connected, Disconnected;
}

public interface OnlineGameComponent {
    public val connectionStatus: StateFlow<ConnectionStatus>
    public val freeRoomIdFlow: SharedFlow<String>
    public val roomDescriptionFlow: SharedFlow<ServerApi.RoomDescription>
    public val gameStateFlow: StateFlow<ServerApi.OnlineGame.State?>
    
    public fun sendSignal(signal: ClientApi.Signal)
    
    public fun resetGameState()
}