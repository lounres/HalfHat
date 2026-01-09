package dev.lounres.halfhat.client.ui.components.game.onlineGame.previewScreen

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.controller.NavigationAction
import dev.lounres.halfhat.client.components.navigation.controller.doStoringNavigation
import dev.lounres.halfhat.client.components.navigation.controller.navigationContext
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


public class RealPreviewScreenComponent(
    componentContext: UIComponentContext,
    override val currentRoomSearchEntry: KoneMutableAsynchronousHub<String>,
    override val currentEnterName: KoneMutableAsynchronousHub<String>,
    onFetchFreeRoomId: () -> Unit,
    roomDescriptionFlow: Flow<ServerApi.RoomDescription>,
    override val onJoinRoom: () -> Unit,
) : PreviewScreenComponent {
    private val coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    override val onChangeRoomSearchEntry: (String) -> Unit = {
        coroutineScope.launch {
            currentRoomPreview.value = PreviewScreenComponent.RoomPreview.Loading
            componentContext.navigationContext.doStoringNavigation(action = NavigationAction.ReplaceState) {
                currentRoomSearchEntry.set(it)
            }
        }
    }
    override val generateRoomSearchEntry: () -> Unit = onFetchFreeRoomId
    
    override val currentRoomPreview: MutableStateFlow<PreviewScreenComponent.RoomPreview> =
        MutableStateFlow(PreviewScreenComponent.RoomPreview.Empty)
    override val onSetEnterName: (String) -> Unit = {
        coroutineScope.launch {
            componentContext.navigationContext.doStoringNavigation(action = NavigationAction.ReplaceState) {
                currentEnterName.set(it)
            }
        }
    }
    
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