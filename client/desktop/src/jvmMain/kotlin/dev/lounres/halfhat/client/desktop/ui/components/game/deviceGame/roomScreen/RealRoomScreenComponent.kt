package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomScreen

import dev.lounres.kone.collections.KoneList
import dev.lounres.kone.collections.buildKoneList
import dev.lounres.kone.collections.toKoneMutableList
import dev.lounres.kone.collections.utils.plusAssign
import dev.lounres.kone.collections.utils.shuffled
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random


class RealRoomScreenComponent(
    override val onExitDeviceGame: () -> Unit,
    override val onOpenGameSettings: () -> Unit,
    override val onStartGame: () -> Unit,
    override val playersList: MutableStateFlow<KoneList<String>>,
) : RoomScreenComponent {
    override val onChangePLayersName: (UInt, String) -> Unit = { index, newName ->
        playersList.update { it.toKoneMutableList().apply { this[index] = newName } }
    }
    override val onRemovePLayer: (UInt) -> Unit = { playerIndex ->
        playersList.update { it.toKoneMutableList().apply { this.removeAt(playerIndex) }}
    }
    override val onAddPLayer: () -> Unit = {
        playersList.update {
            buildKoneList(it.size + 1u) {
                this += it
                this += ""
            }
        }
    }
    override val onShufflePlayersList: () -> Unit = { playersList.update { it.shuffled(Random) } } // TODO: Hardcoded random
}