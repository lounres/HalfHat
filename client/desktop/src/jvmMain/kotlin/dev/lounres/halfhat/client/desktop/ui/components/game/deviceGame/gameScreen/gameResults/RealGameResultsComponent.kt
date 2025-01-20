package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.gameResults

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.flow.StateFlow


class RealGameResultsComponent(
    override val onExitGame: () -> Unit,
    override val results: StateFlow<KoneList<GameStateMachine.PersonalResult<String>>>,
) : GameResultsComponent