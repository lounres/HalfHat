package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomScreen

import dev.lounres.kone.collections.KoneList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeRoomScreenComponent(
    initialPlayersList: KoneList<String> = KoneList(15u) { "player #$it" },
): RoomScreenComponent {
    override val onExitDeviceGame: () -> Unit = {}
    
    override val playersList: StateFlow<KoneList<String>> = MutableStateFlow(initialPlayersList)
    override val onChangePLayersName: (UInt, String) -> Unit = { _, _ -> }
    override val onRemovePLayer: (UInt) -> Unit = {}
    override val onAddPLayer: () -> Unit = {}
    override val onShufflePlayersList: () -> Unit = {}

    override val onOpenGameSettings: () -> Unit = {}
    override val onStartGame: () -> Unit = {}
}