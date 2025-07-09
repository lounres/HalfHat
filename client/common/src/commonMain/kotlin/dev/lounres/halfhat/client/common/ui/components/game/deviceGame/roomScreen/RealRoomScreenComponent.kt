package dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomScreen

import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.list.toKoneMutableList
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.empty
import dev.lounres.kone.collections.utils.shuffled
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random


public class RealRoomScreenComponent(
    override val onExitDeviceGame: () -> Unit,
    override val onOpenGameSettings: () -> Unit,
    override val onStartGame: () -> Unit,
    override val playersList: MutableStateFlow<KoneList<Player>>,
    override val showErrorForPlayers: MutableStateFlow<KoneSet<Player>>
) : RoomScreenComponent {
    override val onChangePLayersName: (UInt, String) -> Unit = { index, newName ->
        showErrorForPlayers.value = KoneSet.empty()
        playersList.update {
            it.toKoneMutableList().apply {
                val oldPlayer = this[index]
                this[index] = Player(name = newName, id = oldPlayer.id)
            }
        }
    }
    override val onRemovePLayer: (UInt) -> Unit = { playerIndex ->
        showErrorForPlayers.value = KoneSet.empty()
        playersList.update {
            check(it.size > 2u) { "Cannot remove player when there are no more than two of them" }
            it.toKoneMutableList().apply { this.removeAt(playerIndex) }
        }
    }
    override val onAddPLayer: () -> Unit = {
        showErrorForPlayers.value = KoneSet.empty()
        playersList.update {
            KoneList.build(it.size + 1u) {
                +it
                +Player("")
            }
        }
    }
    override val onShufflePlayersList: () -> Unit = {
        showErrorForPlayers.value = KoneSet.empty()
        playersList.update { it.shuffled(Random) } // TODO: Hardcoded random
    }
}