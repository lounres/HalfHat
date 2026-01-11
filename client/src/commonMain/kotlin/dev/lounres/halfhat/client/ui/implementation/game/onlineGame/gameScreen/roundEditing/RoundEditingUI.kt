package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundEditing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundEditing.RoundEditingComponent
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.utils.withIndex
import dev.lounres.kone.hub.subscribeAsState


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
                            if (needsSpace) Spacer(modifier = Modifier.height(16.dp))
                            needsSpace = true
                            val (word, state) = wordExplanation
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        modifier = Modifier.height(120.dp),
                                        text = word,
                                        autoSize = TextAutoSize.StepBased(),
                                        softWrap = false,
                                        maxLines = 1,
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    val useDark = component.darkTheme.subscribeAsState().value.isDark
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        ToggleButton(
                                            modifier = Modifier.weight(1f),
                                            checked = state == GameStateMachine.WordExplanation.State.Explained,
                                            onCheckedChange = { if (it) component.onGuessed(roundRole.wordsToEdit, index) },
                                            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                                            colors = ToggleButtonDefaults.toggleButtonColors(
                                                checkedContainerColor =
                                                    if (useDark) Color(0xFFB1D18A)
                                                    else Color(0xFF4C662B),
                                                checkedContentColor =
                                                    if (useDark) Color(0xFF1F3701)
                                                    else Color(0xFFFFFFFF),
                                            ),
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
                                        Spacer(modifier = Modifier.width(4.dp))
                                        ToggleButton(
                                            modifier = Modifier.weight(1f),
                                            checked = state == GameStateMachine.WordExplanation.State.NotExplained,
                                            onCheckedChange = { if (it) component.onNotGuessed(roundRole.wordsToEdit, index) },
                                            shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
                                            colors = ToggleButtonDefaults.toggleButtonColors(
                                                checkedContainerColor =
                                                    if (useDark) Color(0xFFAAC7FF)
                                                    else Color(0xFF415F91),
                                                checkedContentColor =
                                                    if (useDark) Color(0xFF0A305F)
                                                    else Color(0xFFFFFFFF),
                                            ),
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
                                        Spacer(modifier = Modifier.width(4.dp))
                                        ToggleButton(
                                            modifier = Modifier.weight(1f),
                                            checked = state == GameStateMachine.WordExplanation.State.Mistake,
                                            onCheckedChange = { if (it) component.onMistake(roundRole.wordsToEdit, index) },
                                            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                            colors = ToggleButtonDefaults.toggleButtonColors(
                                                checkedContainerColor =
                                                    if (useDark) Color(0xFFFFB5A0)
                                                    else Color(0xFF8F4C38),
                                                checkedContentColor =
                                                    if (useDark) Color(0xFF561F0F)
                                                    else Color(0xFFFFFFFF),
                                            ),
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
public fun RowScope.RoundEditingToolbarUI(
    component: RoundEditingComponent,
) {

}

@Composable
public fun RoundEditingFloatingActionButtonUI(
    component: RoundEditingComponent,
): @Composable (() -> Unit)? = null