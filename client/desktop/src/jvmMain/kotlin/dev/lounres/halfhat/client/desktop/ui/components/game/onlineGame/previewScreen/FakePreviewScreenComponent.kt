package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.previewScreen

import dev.lounres.halfhat.api.server.ServerApi
import dev.lounres.kone.collections.koneListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakePreviewScreenComponent(
    initialRoomSearchEntry: String = "НОЦУЛУРИРЕД",
    initialRoomPreview: PreviewScreenComponent.RoomPreview =
        PreviewScreenComponent.RoomPreview.Present(
            ServerApi.RoomDescription(
                name = initialRoomSearchEntry,
                playersList = koneListOf(
                    ServerApi.PlayerDescription(name = "Полина", isOnline = true),
                    ServerApi.PlayerDescription(name = "Глеб", isOnline = true),
                    ServerApi.PlayerDescription(name = "Ваня", isOnline = true),
                    ServerApi.PlayerDescription(name = "Наиль", isOnline = true),
                ),
                state = ServerApi.RoomStateType.RoundExplanation,
            )
        ),
    initialEnterName: String = "РВ",
) : PreviewScreenComponent {
    override val onExitOnlineGame: () -> Unit = {}
    
    override val currentRoomSearchEntry: StateFlow<String> = MutableStateFlow(initialRoomSearchEntry)
    override val onChangeRoomSearchEntry: (String) -> Unit = {}
    override val generateRoomSearchEntry: () -> Unit = {}
    
    override val currentRoomPreview: StateFlow<PreviewScreenComponent.RoomPreview> = MutableStateFlow(initialRoomPreview)
    override val currentEnterName: MutableStateFlow<String> = MutableStateFlow(initialEnterName)
    override val onJoinRoom: () -> Unit = {}
}