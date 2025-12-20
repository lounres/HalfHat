package dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.gameResults

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.gameResults.GameResultsComponent
import dev.lounres.kone.collections.iterables.next


@Composable
public fun RowScope.GameResultsActionsUI(
    component: GameResultsComponent
) {

}

@Composable
public fun ColumnScope.GameResultsUI(
    component: GameResultsComponent
) {
    val gameState by component.gameState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "Player",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "Explained",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "Guessed",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "Sum",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                for ((player, explainedScore, guessedScore, sum) in gameState.results) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = gameState.playersList[player],
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = explainedScore.toString(),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = guessedScore.toString(),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = sum.toString(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
public fun RowScope.GameResultsButtonsUI(
    component: GameResultsComponent
) {

}