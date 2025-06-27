package dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundExplanation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundExplanation.RoundExplanationComponent
import dev.lounres.halfhat.client.common.ui.utils.GameInProcessTemplate


@Composable
public fun RowScope.RoundExplanationActionsUI(
    component: RoundExplanationComponent,
) {

}

@Composable
public fun ColumnScope.RoundExplanationUI(
    component: RoundExplanationComponent,
) {
    val gameState by component.gameState.collectAsState()
    GameInProcessTemplate(
        speaker = gameState.playersList[gameState.speakerIndex].name,
        listener = gameState.playersList[gameState.listenerIndex].name,
        timer = {
            Text(
                text = (gameState.millisecondsLeft / 1_000u + 1u).let { "${it / 60u}:${it % 60u}" },
                fontSize = 32.sp,
            )
        },
        word = TODO(),
        onGuessed = component.onGuessed,
        onNotGuessed = component.onNotGuessed,
        onMistake = component.onMistake,
    )
}

@Composable
public fun RowScope.RoundExplanationButtonsUI(
    component: RoundExplanationComponent,
) {

}