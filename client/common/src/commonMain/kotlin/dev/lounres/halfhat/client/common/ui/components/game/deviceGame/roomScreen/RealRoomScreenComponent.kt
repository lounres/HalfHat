package dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomScreen

import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.list.toKoneMutableList
import dev.lounres.kone.collections.utils.shuffled
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random


public class RealRoomScreenComponent(
    override val onExitDeviceGame: () -> Unit,
    override val onOpenGameSettings: () -> Unit,
    override val onStartGame: () -> Unit,
    override val playersList: MutableStateFlow<KoneList<Player>>,
    override val showErrorForEmptyPlayerNames: MutableStateFlow<Boolean>
) : RoomScreenComponent {
    override val onChangePLayersName: (UInt, String) -> Unit = { index, newName ->
        showErrorForEmptyPlayerNames.value = false
        playersList.update { it.toKoneMutableList().apply { this[index].name = newName } }
    }
    override val onRemovePLayer: (UInt) -> Unit = { playerIndex ->
        showErrorForEmptyPlayerNames.value = false
        playersList.update { it.toKoneMutableList().apply { this.removeAt(playerIndex) }}
    }
    override val onAddPLayer: () -> Unit = {
        showErrorForEmptyPlayerNames.value = false
        playersList.update {
            KoneList.build(it.size + 1u) {
                +it
                +Player("")
            }
        }
    }
    override val onShufflePlayersList: () -> Unit = {
        showErrorForEmptyPlayerNames.value = false
        playersList.update { it.shuffled(Random) } // TODO: Hardcoded random
    }
}