package dev.lounres.thetruehat.client.desktop.ui.game.roundInProgress

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.lounres.thetruehat.client.desktop.components.game.roundInProgress.FakeRoundInProgressPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.roundInProgress.RoundInProgressPageComponent
import dev.lounres.thetruehat.client.desktop.uiTemplates.RoundOverviewPageTemplateUI
import dev.lounres.thetruehat.client.desktop.uiTemplates.RoundPageTemplateUI


@Preview
@Composable
fun RoundInProgressPageUIPreview() {
    RoundInProgressPageUI(
        component = FakeRoundInProgressPageComponent()
    )
}

@Composable
fun RoundInProgressPageUI(
    component: RoundInProgressPageComponent
) {
    when (val userRole = component.userRole) {
        is RoundInProgressPageComponent.RoundInProgressUserRole.Speaker ->
            RoundPageTemplateUI(
                backButtonEnabled = component.backButtonEnabled,
                wordsNumber = component.wordsNumber,
                volumeOn = component.volumeOn,
                showFinishButton = component.showFinishButton,
                onBackButtonClick = component.onBackButtonClick,
                onLanguageChange = component.onLanguageChange,
                onFeedbackButtonClick = component.onFeedbackButtonClick,
                onHatButtonClick = component.onHatButtonClick,
                onExitButtonClick = component.onExitButtonClick,
                onVolumeButtonClick = component.onVolumeButtonClick,
                onFinishButtonClick = component.onFinishButtonClick,
            ) {
                Text(
                    text = "Ты объясняешь",
                    fontSize = 30.sp,
                )
                Text(
                    text = "${component.listenerNickname} отгадывает",
                    fontSize = 18.sp,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.height(50.dp))
                Text(
                    text = userRole.wordToExplain.subscribeAsState().value,
                    fontSize = 50.sp,
                )
                val timeLeft by component.timeLeft.subscribeAsState()
                Text(
                    text = "${timeLeft / 60}:${timeLeft % 60}",
                    fontSize = 30.sp,
                )
                Spacer(modifier = Modifier.height(50.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO */ },
                    ) {
                        Text(
                            text = "Не угадал",
                            fontSize = 20.sp,
                        )
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO */ },
                    ) {
                        Text(
                            text = "Ошибка",
                            fontSize = 20.sp,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /* TODO */ },
                ) {
                    Text(
                        text = "Угадал",
                        fontSize = 30.sp,
                    )
                }
            }
        RoundInProgressPageComponent.RoundInProgressUserRole.Listener,
        is RoundInProgressPageComponent.RoundInProgressUserRole.SpeakerIn,
        is RoundInProgressPageComponent.RoundInProgressUserRole.ListenerIn ->
            RoundOverviewPageTemplateUI(
                backButtonEnabled = component.backButtonEnabled,
                wordsNumber = component.wordsNumber,
                volumeOn = component.volumeOn,
                showFinishButton = component.showFinishButton,
                speakerNickname = component.speakerNickname,
                listenerNickname = component.listenerNickname,
                onBackButtonClick = component.onBackButtonClick,
                onLanguageChange = component.onLanguageChange,
                onFeedbackButtonClick = component.onFeedbackButtonClick,
                onHatButtonClick = component.onHatButtonClick,
                onExitButtonClick = component.onExitButtonClick,
                onVolumeButtonClick = component.onVolumeButtonClick,
                onFinishButtonClick = component.onFinishButtonClick,
                header = {
                    when (userRole) {
                        is RoundInProgressPageComponent.RoundInProgressUserRole.Speaker -> error("") // TODO: Add error comment
                        RoundInProgressPageComponent.RoundInProgressUserRole.Listener -> {
                            Text(
                                text = "Ты отгадываешь",
                                fontSize = 30.sp,
                            )
                        }
                        is RoundInProgressPageComponent.RoundInProgressUserRole.SpeakerIn -> {
                            Text(
                                text = "Подготовка",
                                fontSize = 30.sp,
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Ты объясняешь через ${userRole.rounds} ходов",
                                fontSize = 18.sp,
                                color = Color.Gray,
                            )
                        }
                        is RoundInProgressPageComponent.RoundInProgressUserRole.ListenerIn -> {
                            Text(
                                text = "Подготовка",
                                fontSize = 30.sp,
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Ты отгадываешь через ${userRole.rounds} ходов",
                                fontSize = 18.sp,
                                color = Color.Gray,
                            )
                        }
                    }
                },
                footer = {
                    val timeLeft by component.timeLeft.subscribeAsState()
                    Text(
                        text = "${timeLeft / 60}:${timeLeft % 60}",
                        fontSize = 30.sp,
                    )
                }
            )
    }
}