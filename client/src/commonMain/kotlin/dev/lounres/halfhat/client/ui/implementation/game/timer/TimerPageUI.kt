package dev.lounres.halfhat.client.ui.implementation.game.timer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.logic.components.game.timer.TimerState
import dev.lounres.halfhat.client.logic.components.game.timer.represent
import dev.lounres.halfhat.client.ui.components.game.timer.TimerPageComponent
import dev.lounres.halfhat.client.ui.icons.GameTimerExitModeButton
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.kone.hub.set
import dev.lounres.kone.hub.subscribeAsState
import kotlinx.coroutines.launch
import kotlin.math.min


@Composable
public fun RowScope.TimerPageActionsUI(
    component: TimerPageComponent
) {
    IconButton(
        onClick = component.onExitTimer
    ) {
        Icon(
            modifier = commonIconModifier,
            imageVector = HalfHatIcon.GameTimerExitModeButton,
            contentDescription = "Exit game timer"
        )
    }
}

@Composable
public fun TimerPageUI(
    component: TimerPageComponent
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().widthIn(max = 480.dp).align(Alignment.Center).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val timerState by component.timerState.subscribeAsState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                when (val timerStateValue = timerState) {
                    TimerState.Finished -> {
                        val preparationTime by component.preparationTimeSetting.subscribeAsState()
                        val explanationTime by component.explanationTimeSetting.subscribeAsState()
                        val lastGuessTime by component.lastGuessTimeSetting.subscribeAsState()
                        
                        val coroutineScope = rememberCoroutineScope()
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = preparationTime,
                                onValueChange = { input ->
                                    coroutineScope.launch {
                                        component.preparationTimeSetting
                                            .set(input.filter { it.isDigit() }.let { if (it.isEmpty()) "" else it.dropWhile { d -> d == '0' }.ifBlank { "0" }.let { if (it.length > 3) "999" else it } })
                                    }
                                },
                                label = { Text(text = "Countdown duration") },
                                suffix = { Text(text = " seconds") },
                                singleLine = true,
                                isError = preparationTime.isBlank(),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Set 0 to disable the stage.",
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = explanationTime,
                                onValueChange = { input ->
                                    coroutineScope.launch {
                                        component.explanationTimeSetting
                                            .set(input.filter { it.isDigit() }.let { if (it.isEmpty()) "" else it.dropWhile { d -> d == '0' }.ifBlank { "0" }.let { if (it.length > 3) "999" else it } })
                                    }
                                },
                                label = { Text(text = "Explanation duration") },
                                suffix = { Text(text = " seconds") },
                                singleLine = true,
                                isError = explanationTime.isBlank(),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Set 0 to disable the stage.",
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = lastGuessTime,
                                onValueChange = { input ->
                                    coroutineScope.launch {
                                        component.lastGuessTimeSetting
                                            .set(input.filter { it.isDigit() }.let { if (it.isEmpty()) "" else it.dropWhile { d -> d == '0' }.ifBlank { "0" }.let { if (it.length > 3) "999" else it } })
                                    }
                                },
                                label = { Text(text = "Last guess duration") },
                                suffix = { Text(text = " seconds") },
                                singleLine = true,
                                isError = lastGuessTime.isBlank(),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Set 0 to disable the stage.",
                            )
                        }
                    }
                    
                    is TimerState.Preparation ->
                        Text(
                            text = timerStateValue.represent(),
                            fontSize = 256.sp,
                            color = Color.hsv(
                                hue = min(timerStateValue.millisecondsLeft, 3_000u).toInt() * 0.04f,
                                saturation = 1f,
                                value = 1f,
                            ),
                        )
                    
                    is TimerState.Explanation ->
                        Text(
                            text = timerStateValue.represent(),
                            fontSize = 128.sp,
                        )
                    
                    is TimerState.LastGuess ->
                        Text(
                            text = timerStateValue.represent(),
                            fontSize = 128.sp,
                            color = Color.Red,
                        )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                modifier = Modifier.fillMaxWidth(0.4847561f),
                onClick = {
                    when (timerState) {
                        TimerState.Finished -> component.onStartTimer()
                        is TimerState.Preparation,
                        is TimerState.Explanation,
                        is TimerState.LastGuess,
                            -> component.onResetTimer()
                    }
                },
                shape = CircleShape,
            ) {
                Text(
                    text = when (timerState) {
                        TimerState.Finished -> "START"
                        is TimerState.Preparation,
                        is TimerState.Explanation,
                        is TimerState.LastGuess,
                            -> "RESET"
                    },
                    fontSize = 24.sp,
                )
            }
        }
    }
}