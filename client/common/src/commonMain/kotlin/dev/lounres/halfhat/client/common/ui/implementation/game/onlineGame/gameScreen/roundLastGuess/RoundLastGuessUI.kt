package dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundLastGuess

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessComponent
import dev.lounres.halfhat.client.common.ui.utils.GameInProcessTemplate


@Composable
public fun RowScope.RoundLastGuessActionsUI(
    component: RoundLastGuessComponent,
) {

}

@Composable
public fun ColumnScope.RoundLastGuessUI(
    component: RoundLastGuessComponent,
) {
    val gameState by component.gameState.collectAsState()
    GameInProcessTemplate(
        speaker = gameState.playersList[gameState.speakerIndex].name,
        listener = gameState.playersList[gameState.listenerIndex].name,
        timer = {
            Text(
                text = (gameState.millisecondsLeft / 100u + 1u).let { "${it / 10u}.${it % 10u}" },
                fontSize = 64.sp,
                color = Color.Red
            )
        },
        word = TODO(),
        onGuessed = component.onGuessed,
        onNotGuessed = component.onNotGuessed,
        onMistake = component.onMistake,
    )
}

@Composable
public fun RowScope.RoundLastGuessButtonsUI(
    component: RoundLastGuessComponent,
) {

}