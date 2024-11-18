package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.gameResults

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.KoneList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random
import kotlin.random.nextUInt


class FakeGameResultsComponent(
    initialResults: KoneList<GameStateMachine.PersonalResult<String>> =
        KoneList(10u) {
            GameStateMachine.PersonalResult(
                player = "player #$it",
                scoreExplained = Random.nextUInt(0u, 10u),
                scoreGuessed = Random.nextUInt(0u, 10u),
                sum = Random.nextUInt(0u, 10u),
            )
        }
) : GameResultsComponent {
    override val onExitGame: () -> Unit = {}
    
    override val results: StateFlow<KoneList<GameStateMachine.PersonalResult<String>>> = MutableStateFlow(initialResults)
}