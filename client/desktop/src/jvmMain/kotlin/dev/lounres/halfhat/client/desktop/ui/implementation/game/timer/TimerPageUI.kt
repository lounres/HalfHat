package dev.lounres.halfhat.client.desktop.ui.implementation.game.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.desktop.logic.timer.State
import dev.lounres.halfhat.client.desktop.resources.exitGameTimerButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import dev.lounres.halfhat.client.desktop.ui.components.game.timer.TimerPageComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
fun RowScope.TimerPageActionsUI(
    component: TimerPageComponent
) {
    IconButton(
        onClick = component.onExitTimer
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.exitGameTimerButton_dark_png_24dp),
            contentDescription = "Exit device game"
        )
    }
}

@Composable
fun TimerPageUI(
    component: TimerPageComponent
) {
    val timerState by component.timerState.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when(val timerStateValue = timerState) {
                State.Finished -> {
                    val preparationTime by component.preparationTimeSetting.collectAsState()
                    val explanationTime by component.explanationTimeSetting.collectAsState()
                    val lastGuessTime by component.lastGuessTimeSetting.collectAsState()
                    
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = preparationTime,
                        onValueChange = { component.preparationTimeSetting.value = it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" } },
                        label = { androidx.compose.material3.Text(text = "Countdown duration") },
                        suffix = { androidx.compose.material3.Text(text = " seconds") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = explanationTime,
                        onValueChange = { component.explanationTimeSetting.value = it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" } },
                        label = { androidx.compose.material3.Text(text = "Explanation duration") },
                        suffix = { androidx.compose.material3.Text(text = " seconds") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = lastGuessTime,
                        onValueChange = { component.lastGuessTimeSetting.value = it.filter { it.isDigit() }.dropWhile { it == '0' }.ifEmpty { "0" } },
                        label = { androidx.compose.material3.Text(text = "Last guess duration") },
                        suffix = { androidx.compose.material3.Text(text = " seconds") },
                        singleLine = true,
                    )
                }
                is State.Preparation ->
                    Text(
                        text = timerStateValue.represent(),
                        fontSize = 256.sp,
                        color = Color.Red, // TODO: Replace with gradually changed color
                    )
                is State.Explanation ->
                    Text(
                        text = timerStateValue.represent(),
                        fontSize = 128.sp,
                    )
                is State.LastGuess ->
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
                    State.Finished -> component.onStartTimer()
                    is State.Preparation,
                    is State.Explanation,
                    is State.LastGuess -> component.onResetTimer()
                }
            },
            shape = CircleShape,
        ) {
            Text(
                text = when (timerState) {
                    State.Finished -> "START"
                    is State.Preparation,
                    is State.Explanation,
                    is State.LastGuess -> "RESET"
                },
                fontSize = 24.sp,
            )
        }
    }
}