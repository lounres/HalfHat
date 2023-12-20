package dev.lounres.thetruehat.client.common.ui.game.roundInProgress

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
import dev.lounres.thetruehat.client.common.components.game.roundInProgress.RoundInProgressPageComponent
import dev.lounres.thetruehat.client.common.uiTemplates.RoundOverviewPageTemplateUI
import dev.lounres.thetruehat.client.common.uiTemplates.RoundPageTemplateUI


//@Preview
//@Composable
//fun RoundInProgressPageUIPreview() {
//    RoundInProgressPageUI(
//        component = FakeRoundInProgressPageComponent()
//    )
//}

@Composable
public fun RoundInProgressPageUI(
    component: RoundInProgressPageComponent
) {
    when (val userRole = component.userRole.subscribeAsState().value) {
        is RoundInProgressPageComponent.UserRole.Speaker ->
            RoundPageTemplateUI(
                backButtonEnabled = component.backButtonEnabled,
                unitsUntilEnd = component.unitsUntilEnd,
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
                    text = "${component.listenerNickname.subscribeAsState().value} отгадывает",
                    fontSize = 18.sp,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.height(50.dp))
                Text(
                    text = userRole.wordToExplain.subscribeAsState().value,
                    fontSize = 50.sp,
                )
                val timeLeft by component.countsUntilEnd.subscribeAsState()
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
                        onClick = component.onNotExplainedButtonClick,
                    ) {
                        Text(
                            text = "Не угадал",
                            fontSize = 20.sp,
                        )
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = component.onImproperlyExplainedButtonClick,
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
                    onClick = component.onExplainedButtonClick,
                ) {
                    Text(
                        text = "Угадал",
                        fontSize = 30.sp,
                    )
                }
            }
        RoundInProgressPageComponent.UserRole.Listener,
        is RoundInProgressPageComponent.UserRole.SpeakerIn,
        is RoundInProgressPageComponent.UserRole.ListenerIn ->
            RoundOverviewPageTemplateUI(
                backButtonEnabled = component.backButtonEnabled,
                unitsUntilEnd = component.unitsUntilEnd,
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
                    @Suppress("KotlinConstantConditions")
                    when (userRole) {
                        is RoundInProgressPageComponent.UserRole.Speaker -> error("") // TODO: Add error comment
                        RoundInProgressPageComponent.UserRole.Listener -> {
                            Text(
                                text = "Ты отгадываешь",
                                fontSize = 30.sp,
                            )
                        }
                        is RoundInProgressPageComponent.UserRole.SpeakerIn -> {
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
                        is RoundInProgressPageComponent.UserRole.ListenerIn -> {
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
                    val timeLeft by component.countsUntilEnd.subscribeAsState()
                    Text(
                        text = "${timeLeft / 60}:${timeLeft % 60}",
                        fontSize = 30.sp,
                    )
                }
            )
    }
}