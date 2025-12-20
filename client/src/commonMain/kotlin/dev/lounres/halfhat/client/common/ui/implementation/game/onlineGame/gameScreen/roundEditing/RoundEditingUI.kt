package dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundEditing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundEditing.RoundEditingComponent
import dev.lounres.halfhat.client.common.ui.utils.AutoScalingText
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.utils.withIndex


@Composable
public fun RowScope.RoundEditingActionsUI(
    component: RoundEditingComponent,
) {

}

@Composable
public fun ColumnScope.RoundEditingUI(
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
        
        val role = component.gameState.collectAsState().value.role
        
        when (val roundRole = role.roundRole) {
            ServerApi.OnlineGame.Role.RoundEditing.RoundRole.Player -> {}
            ServerApi.OnlineGame.Role.RoundEditing.RoundRole.Listener -> {}
            is ServerApi.OnlineGame.Role.RoundEditing.RoundRole.Speaker -> {
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
                        for ((index, wordExplanation) in roundRole.wordsToEdit.withIndex()) {
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
                                        onClick = { component.onGuessed(roundRole.wordsToEdit, index) },
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
                                        onClick = { component.onNotGuessed(roundRole.wordsToEdit, index) },
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
                                        onClick = { component.onMistake(roundRole.wordsToEdit, index) },
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
    }
}

@Composable
public fun RowScope.RoundEditingButtonsUI(
    component: RoundEditingComponent,
) {

}