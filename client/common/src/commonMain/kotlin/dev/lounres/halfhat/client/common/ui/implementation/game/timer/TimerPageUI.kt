package dev.lounres.halfhat.client.common.ui.implementation.game.timer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.common.logic.components.game.timer.TimerState
import dev.lounres.halfhat.client.common.logic.components.game.timer.represent
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.exitGameTimerButton_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.game.timer.TimerPageComponent
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import dev.lounres.kone.state.subscribeAsState
import org.jetbrains.compose.resources.painterResource
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
            painter = painterResource(Res.drawable.exitGameTimerButton_dark_png_24dp),
            contentDescription = "Exit device game"
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
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = preparationTime.toString(),
                                onValueChange = {
                                    component.preparationTimeSetting.value =
                                        it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" }.toUInt().let { min(it, 999u) }
                                },
                                label = { Text(text = "Countdown duration") },
                                suffix = { Text(text = " seconds") },
                                singleLine = true,
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
                                value = explanationTime.toString(),
                                onValueChange = {
                                    component.explanationTimeSetting.value =
                                        it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" }.toUInt().let { min(it, 999u) }
                                },
                                label = { Text(text = "Explanation duration") },
                                suffix = { Text(text = " seconds") },
                                singleLine = true,
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
                                value = lastGuessTime.toString(),
                                onValueChange = {
                                    component.lastGuessTimeSetting.value =
                                        it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" }.toUInt().let { min(it, 999u) }
                                },
                                label = { Text(text = "Last guess duration") },
                                suffix = { Text(text = " seconds") },
                                singleLine = true,
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