package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundEditing

import dev.lounres.halfhat.api.server.ServerApi
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
            userIndex = 0u,
            settings = ServerApi.Settings(
                preparationTimeSeconds = 0u,
                explanationTimeSeconds = 0u,
                finalGuessTimeSeconds = 0u,
                strictMode = true,
                gameEndCondition = GameStateMachine.GameEndCondition.Words(100u),
            ),
            roundNumber = 0u,
            cycleNumber = 0u,
            speakerIndex = 0u,
            listenerIndex = 0u,
            explanationScores = KoneList.empty(),
            guessingScores = KoneList.empty(),
            wordsToEdit = KoneList.empty(),
        ),
) : RoundEditingComponent {
    override val gameState: StateFlow<ServerApi.OnlineGame.State.RoundEditing> = MutableStateFlow(initialGameState)
    
    override val onGuessed: (UInt) -> Unit = {}
    override val onNotGuessed: (UInt) -> Unit = {}
    override val onMistake: (UInt) -> Unit = {}
    
    override val onConfirm: () -> Unit = {}
}