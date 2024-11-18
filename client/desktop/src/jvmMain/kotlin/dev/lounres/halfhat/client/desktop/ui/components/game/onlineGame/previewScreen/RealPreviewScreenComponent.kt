package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.previewScreen

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.lounres.halfhat.api.server.ServerApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class RealPreviewScreenComponent(
    componentContext: ComponentContext,
    override val onExitOnlineGame: () -> Unit,
    onFetchFreeRoomId: () -> Unit,
    freeRoomIdFlow: Flow<String>,
    onFetchRoomInfo: (roomId: String) -> Unit,
    roomDescriptionFlow: Flow<ServerApi.RoomDescription>,
    onEnterRoom: (roomId: String, playerName: String) -> Unit,
) : PreviewScreenComponent {
    override val currentRoomSearchEntry: MutableStateFlow<String> = MutableStateFlow("")
    override val onChangeRoomSearchEntry: (String) -> Unit = {
        currentRoomSearchEntry.value = it
        currentRoomPreview.value = PreviewScreenComponent.RoomPreview.Loading
        onFetchRoomInfo(it)
    }
    override val generateRoomSearchEntry: () -> Unit = onFetchFreeRoomId
    
    override val currentRoomPreview: MutableStateFlow<PreviewScreenComponent.RoomPreview> =
        MutableStateFlow(PreviewScreenComponent.RoomPreview.Empty)
    override val currentEnterName: MutableStateFlow<String> = MutableStateFlow("")
    override val onJoinRoom: () -> Unit = { onEnterRoom(currentRoomSearchEntry.value, currentEnterName.value) }
    
    init {
        with(componentContext.coroutineScope(Dispatchers.Default)) {
            launch {
                freeRoomIdFlow.collect { currentRoomSearchEntry.value = it }
            }
            launch {
                roomDescriptionFlow.collect {
                    currentRoomPreview.value = PreviewScreenComponent.RoomPreview.Present(it)
                }
            }
        }
    }
}