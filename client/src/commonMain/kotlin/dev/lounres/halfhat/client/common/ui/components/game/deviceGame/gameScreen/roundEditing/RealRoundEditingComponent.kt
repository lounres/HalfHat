package dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundEditing

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.flow.StateFlow


public class RealRoundEditingComponent(
    override val onExitGame: () -> Unit,
    override val wordsToEdit: StateFlow<KoneList<GameStateMachine.WordExplanation>>,
    override val onGuessed: (UInt) -> Unit,
    override val onNotGuessed: (UInt) -> Unit,
    override val onMistake: (UInt) -> Unit,
    override val onConfirm: () -> Unit,
) : RoundEditingComponent