package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundEditing

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.KoneList
import kotlinx.coroutines.flow.StateFlow


class RealRoundEditingComponent(
    override val onExitGame: () -> Unit,
    override val wordsToEdit: StateFlow<KoneList<GameStateMachine.WordExplanation>>,
    override val onGuessed: (UInt) -> Unit,
    override val onNotGuessed: (UInt) -> Unit,
    override val onMistake: (UInt) -> Unit,
    override val onConfirm: () -> Unit,
) : RoundEditingComponent