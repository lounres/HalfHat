package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundEditing

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.empty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class FakeRoundEditingComponent(
    initialGameState: ServerApi.OnlineGame.State.RoundEditing =
        ServerApi.OnlineGame.State.RoundEditing(
            role = TODO(),
            playersList = KoneList.empty(),
            settings = ServerApi.Settings(
                preparationTimeSeconds = 0u,
                explanationTimeSeconds = 0u,
                finalGuessTimeSeconds = 0u,
                strictMode = true,
                gameEndCondition = GameStateMachine.GameEndCondition.Words(100u),
                wordsSource = TODO()
            ),
            roundNumber = 0u,
            cycleNumber = 0u,
            speakerIndex = 0u,
            listenerIndex = 0u,
            explanationScores = KoneList.empty(),
            guessingScores = KoneList.empty(),
        ),
) : RoundEditingComponent {
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundEditing> = MutableStateFlow(initialGameState)
    
    override val onGuessed: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit = { _, _ -> }
    override val onNotGuessed: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit = { _, _ -> }
    override val onMistake: (KoneList<GameStateMachine.WordExplanation>, UInt) -> Unit = { _, _ -> }
    
    override val onConfirm: () -> Unit = {}
}