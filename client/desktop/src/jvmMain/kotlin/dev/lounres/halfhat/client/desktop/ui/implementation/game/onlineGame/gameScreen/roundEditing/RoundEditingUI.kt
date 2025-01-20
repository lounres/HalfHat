package dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roundEditing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import dev.lounres.halfhat.client.desktop.resources.exitDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.onlineGameKey_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.onlineGameLink_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundEditing.RoundEditingComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import dev.lounres.halfhat.client.desktop.ui.utils.AutoScalingText
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.utils.withIndex
import org.jetbrains.compose.resources.painterResource


@Composable
fun RowScope.RoundEditingActionsUI(
    component: RoundEditingComponent,
) {
    IconButton(
        onClick = component.onCopyOnlineGameKey
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.onlineGameKey_dark_png_24dp),
            contentDescription = "Copy online game key"
        )
    }
    IconButton(
        onClick = component.onCopyOnlineGameLink
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.onlineGameLink_dark_png_24dp),
            contentDescription = "Copy online game link"
        )
    }
    IconButton(
        onClick = component.onExitOnlineGame
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.exitDeviceGameButton_dark_png_24dp), // TODO: Copy the icons
            contentDescription = "Exit online game"
        )
    }
}

@Composable
fun RoundEditingUI(
    component: RoundEditingComponent,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Round Editing",
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
            ) {
                var needsSpace = false
                for ((index, wordExplanation) in component.gameState.collectAsState().value.wordsToEdit.withIndex()) {
                    if (needsSpace) Spacer(modifier = Modifier.height(8.dp))
                    needsSpace = true
                    val (word, state) = wordExplanation
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AutoScalingText(
                                modifier = Modifier.height(128.dp),
                                text = word,
                                softWrap = false,
                                maxLines = 1,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                shape = CircleShape,
                                enabled = state != GameStateMachine.WordExplanation.State.Explained,
                                onClick = { component.onGuessed(index) },
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = "Guessed",
                                        fontSize = 16.sp,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                shape = CircleShape,
                                enabled = state != GameStateMachine.WordExplanation.State.NotExplained,
                                onClick = { component.onNotGuessed(index) },
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = "Not guessed",
                                        fontSize = 16.sp,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                shape = CircleShape,
                                enabled = state != GameStateMachine.WordExplanation.State.Mistake,
                                onClick = { component.onMistake(index) },
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = "Mistake",
                                        fontSize = 16.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            onClick = component.onConfirm
        ) {
            Text(
                text = "Confirm",
                fontSize = 32.sp
            )
        }
    }
}