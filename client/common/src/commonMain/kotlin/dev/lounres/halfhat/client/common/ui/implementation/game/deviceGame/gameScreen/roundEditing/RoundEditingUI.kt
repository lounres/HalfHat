package dev.lounres.halfhat.client.common.ui.implementation.game.deviceGame.gameScreen.roundEditing

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.exitDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundEditing.RoundEditingComponent
import dev.lounres.halfhat.client.common.ui.utils.AutoScalingText
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.utils.withIndex
import org.jetbrains.compose.resources.painterResource


@Composable
public fun RowScope.RoundEditingActionsUI(
    component: RoundEditingComponent,
) {
    IconButton(
        onClick = component.onExitGame
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(Res.drawable.exitDeviceGameButton_dark_png_24dp),
            contentDescription = "Exit device game"
        )
    }
}

@Composable
public fun RoundEditingUI(
    component: RoundEditingComponent,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().widthIn(max = 480.dp).align(Alignment.Center).padding(16.dp),
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
                    for ((index, wordExplanation) in component.wordsToEdit.collectAsState().value.withIndex()) {
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
                            
                            // TODO: Use row of buttons for wider screens
//                            Column(
//                                modifier = Modifier.fillMaxWidth().padding(8.dp),
//                                horizontalAlignment = Alignment.CenterHorizontally,
//                            ) {
//                                Text( // TODO: Add automatic size setting
//                                    text = word,
//                                    fontSize = 64.sp,
//                                )
//                                Spacer(modifier = Modifier.height(8.dp))
//                                Row(
//                                    modifier = Modifier.fillMaxWidth()
//                                ) {
//                                    Button(
//                                        modifier = Modifier.weight(1f),
//                                        shape = CircleShape,
//                                        enabled = state != WordExplanation.State.Explained,
//                                        onClick = { component.onGuessed(index) },
//                                    ) {
//                                        Row(
//                                            modifier = Modifier.fillMaxSize(),
//                                            verticalAlignment = Alignment.CenterVertically,
//                                            horizontalArrangement = Arrangement.Center,
//                                        ) {
//                                            Text(
//                                                text = "Guessed",
//                                                fontSize = 8.sp,
//                                            )
//                                        }
//                                    }
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Button(
//                                        modifier = Modifier.weight(1f),
//                                        shape = CircleShape,
//                                        enabled = state != WordExplanation.State.NotExplained,
//                                        onClick = { component.onNotGuessed(index) },
//                                    ) {
//                                        Row(
//                                            modifier = Modifier.fillMaxSize(),
//                                            verticalAlignment = Alignment.CenterVertically,
//                                            horizontalArrangement = Arrangement.Center,
//                                        ) {
//                                            Text(
//                                                text = "Not guessed",
//                                                fontSize = 8.sp,
//                                            )
//                                        }
//                                    }
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Button(
//                                        modifier = Modifier.weight(1f),
//                                        shape = CircleShape,
//                                        enabled = state != WordExplanation.State.Mistake,
//                                        onClick = { component.onMistake(index) },
//                                    ) {
//                                        Row(
//                                            modifier = Modifier.fillMaxSize(),
//                                            verticalAlignment = Alignment.CenterVertically,
//                                            horizontalArrangement = Arrangement.Center,
//                                        ) {
//                                            Text(
//                                                text = "Mistake",
//                                                fontSize = 8.sp,
//                                            )
//                                        }
//                                    }
//                                }
//                            }
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
}