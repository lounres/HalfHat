package dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.gameResults

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lounres.halfhat.client.desktop.resources.exitDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.gameResults.GameResultsComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import dev.lounres.kone.collections.iterables.next
import org.jetbrains.compose.resources.painterResource
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes


@Composable
fun RowScope.GameResultsActionsUI(
    component: GameResultsComponent
) {
    IconButton(
        onClick = component.onExitGame
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.exitDeviceGameButton_dark_png_24dp),
            contentDescription = "Exit device game"
        )
    }
}

@Composable
fun GameResultsUI(
    component: GameResultsComponent
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 480.dp)
                .align(Alignment.Center)
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
                    for ((player, explainedScore, guessedScore, sum) in component.results.collectAsState().value) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = player,
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
}