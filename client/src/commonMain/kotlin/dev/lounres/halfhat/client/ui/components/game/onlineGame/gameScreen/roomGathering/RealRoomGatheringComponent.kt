package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roomGathering

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.api.onlineGame.DictionaryId
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.scope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


public class RealRoomGatheringComponent(
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoomPlayersGathering>,
    
    override val onExitOnlineGame: () -> Unit,
    override val onCopyOnlineGameKey: () -> Unit,
    override val onCopyOnlineGameLink: () -> Unit,
    
    override val onFixRoom: () -> Unit,
) : RoomGatheringComponent