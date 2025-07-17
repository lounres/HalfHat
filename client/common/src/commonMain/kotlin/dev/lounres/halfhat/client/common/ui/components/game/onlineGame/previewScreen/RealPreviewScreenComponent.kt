package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.previewScreen

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


public class RealPreviewScreenComponent(
    componentContext: UIComponentContext,
    override val currentRoomSearchEntry: KoneMutableAsynchronousHub<String>,
    onFetchFreeRoomId: () -> Unit,
    onFetchRoomInfo: (roomId: String) -> Unit,
    roomDescriptionFlow: Flow<ServerApi.RoomDescription>,
    onEnterRoom: (roomId: String, playerName: String) -> Unit,
) : PreviewScreenComponent {
    private val coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    override val onChangeRoomSearchEntry: (String) -> Unit = {
        coroutineScope.launch {
            currentRoomSearchEntry.set(it)
            currentRoomPreview.value = PreviewScreenComponent.RoomPreview.Loading
            onFetchRoomInfo(it)
        }
    }
    override val generateRoomSearchEntry: () -> Unit = onFetchFreeRoomId
    
    override val currentRoomPreview: MutableStateFlow<PreviewScreenComponent.RoomPreview> =
        MutableStateFlow(PreviewScreenComponent.RoomPreview.Empty)
    override val currentEnterName: MutableStateFlow<String> = MutableStateFlow("")
    override val onJoinRoom: () -> Unit = { onEnterRoom(currentRoomSearchEntry.value, currentEnterName.value) }
    
    init {
        with(coroutineScope) {
            launch {
                roomDescriptionFlow.collect {
                    currentRoomPreview.value = PreviewScreenComponent.RoomPreview.Present(it)
                }
            }
        }
    }
}