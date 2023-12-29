package dev.lounres.thetruehat.client.desktop.ui.gameTimer.timer

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.lounres.thetruehat.client.common.components.gameTimer.timer.FakeTimerPageComponent
import dev.lounres.thetruehat.client.common.components.gameTimer.timer.TimerPageComponent
import dev.lounres.thetruehat.client.desktop.uiTemplates.TheTrueHatPageWithHatTemplateUI


@Preview
@Composable
fun TimerPageUIPreview() {
    TimerPageUI(
        component = FakeTimerPageComponent()
    )
}

@Composable
fun TimerPageUI(
    component: TimerPageComponent,
) {
    TheTrueHatPageWithHatTemplateUI(
        backButtonEnabled = component.backButtonEnabled,
        onBackButtonClick = component.onBackButtonClick,
        onLanguageChange = component.onLanguageChange,
        onFeedbackButtonClick = component.onFeedbackButtonClick,
        onHatButtonClick = component.onHatButtonClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(450.dp)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when(val entry = component.timeLeft.subscribeAsState().value) {
                is TimerPageComponent.TimerEntry.Countdown ->
                    Text(
                        text = "${entry.timeLeft % 60}",
                        fontSize = 130.sp,
                    )
                is TimerPageComponent.TimerEntry.Explanation ->
                    Text(
                        text = "${entry.timeLeft / 60}:${(entry.timeLeft % 60).toString().padStart(2, '0')}",
                        fontSize = 130.sp,
                    )
                is TimerPageComponent.TimerEntry.FinalGuess ->
                    Text(
                        text = "${entry.timeLeft / 10}.${entry.timeLeft % 10}",
                        fontSize = 130.sp,
                        color = Color.Red
                    )
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = component.onResetButtonClick,
            ) {
                Text(
                    text = "Сброс",
                    fontSize = 36.sp,
                )
            }
        }
    }
}