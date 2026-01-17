package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundEditing.speaker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.speaker.RoundEditingSpeakerContentComponent
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.utils.withIndex
import dev.lounres.kone.hub.subscribeAsState


@Composable
fun RoundEditingSpeakerContentUI(
    component: RoundEditingSpeakerContentComponent
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            val roundRole = component.userRole.collectAsState().value
            for ((index, wordExplanation) in roundRole.wordsToEdit.withIndex()) {
                if (index != 0u) Spacer(modifier = Modifier.height(16.dp))
                val (word, state) = wordExplanation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = word,
                                autoSize = TextAutoSize.StepBased(),
                                softWrap = false,
                                maxLines = 1,
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val useDark = component.darkTheme.subscribeAsState().value.isDark
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ToggleButton(
                                modifier = Modifier.weight(1f),
                                checked = state == GameStateMachine.WordExplanation.State.Explained,
                                onCheckedChange = { if (it) component.onGuessed(index) },
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
                                        autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 16.sp),
                                        softWrap = false,
                                        maxLines = 1,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            ToggleButton(
                                modifier = Modifier.weight(1f),
                                checked = state == GameStateMachine.WordExplanation.State.NotExplained,
                                onCheckedChange = { if (it) component.onNotGuessed(index) },
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
                                        autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 16.sp),
                                        softWrap = false,
                                        maxLines = 1,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            ToggleButton(
                                modifier = Modifier.weight(1f),
                                checked = state == GameStateMachine.WordExplanation.State.Mistake,
                                onCheckedChange = { if (it) component.onMistake(index) },
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
                                        autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 16.sp),
                                        softWrap = false,
                                        maxLines = 1,
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