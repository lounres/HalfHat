package dev.lounres.thetruehat.client.desktop.ui.game.roundBreak

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.lounres.thetruehat.client.desktop.components.game.roundBreak.FakeRoundBreakPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.roundBreak.RoundBreakPageComponent
import dev.lounres.thetruehat.client.desktop.uiTemplates.RoundOverviewPageTemplateUI


@Preview
@Composable
fun RoundBreakPageUIPreview1() {
    RoundBreakPageUI(
        component = FakeRoundBreakPageComponent(
            userRole = RoundBreakPageComponent.UserRole.SpeakerReady
        )
    )
}

@Preview
@Composable
fun RoundBreakPageUIPreview2() {
    RoundBreakPageUI(
        component = FakeRoundBreakPageComponent(
            userRole = RoundBreakPageComponent.UserRole.SpeakerWaiting
        )
    )
}

@Preview
@Composable
fun RoundBreakPageUIPreview3() {
    RoundBreakPageUI(
        component = FakeRoundBreakPageComponent(
            userRole = RoundBreakPageComponent.UserRole.SpeakerIn(3u)
        )
    )
}

@Composable
fun RoundBreakPageUI(
    component: RoundBreakPageComponent,
) {
    RoundOverviewPageTemplateUI(
        backButtonEnabled = component.backButtonEnabled,
        wordsNumber = component.wordsNumber.subscribeAsState().value,
        volumeOn = component.volumeOn.subscribeAsState().value,
        showFinishButton = component.showFinishButton.subscribeAsState().value,
        speakerNickname = component.speakerNickname.subscribeAsState().value,
        listenerNickname = component.listenerNickname.subscribeAsState().value,
        onBackButtonClick = component.onBackButtonClick,
        onLanguageChange = component.onLanguageChange,
        onFeedbackButtonClick = component.onFeedbackButtonClick,
        onHatButtonClick = component.onHatButtonClick,
        onExitButtonClick = component.onExitButtonClick,
        onVolumeButtonClick = component.onVolumeButtonClick,
        onFinishButtonClick = component.onFinishButtonClick,
        header = {
            when (val userRole = component.userRole) {
                RoundBreakPageComponent.UserRole.SpeakerWaiting, RoundBreakPageComponent.UserRole.SpeakerReady -> {
                    Text(
                        text = "Ты объясняешь",
                        fontSize = 30.sp,
                    )
                }
                RoundBreakPageComponent.UserRole.ListenerWaiting, RoundBreakPageComponent.UserRole.ListenerReady -> {
                    Text(
                        text = "Ты отгадываешь",
                        fontSize = 30.sp,
                    )
                }
                is RoundBreakPageComponent.UserRole.SpeakerIn -> {
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
                is RoundBreakPageComponent.UserRole.ListenerIn -> {
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
            when(val userRole = component.userRole) {
                RoundBreakPageComponent.UserRole.SpeakerWaiting,
                RoundBreakPageComponent.UserRole.ListenerWaiting,
                RoundBreakPageComponent.UserRole.SpeakerReady,
                RoundBreakPageComponent.UserRole.ListenerReady ->
                    Button(
                        enabled = userRole == RoundBreakPageComponent.UserRole.SpeakerWaiting || userRole == RoundBreakPageComponent.UserRole.ListenerWaiting,
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        onClick = {},
                    ) {
                        Text(
                            when (userRole) {
                                RoundBreakPageComponent.UserRole.SpeakerWaiting -> "Я готов объяснять"
                                RoundBreakPageComponent.UserRole.ListenerWaiting -> "Я готов отгадывать"
                                RoundBreakPageComponent.UserRole.SpeakerReady,
                                RoundBreakPageComponent.UserRole.ListenerReady -> "Подожди напарника"
                                else -> error("There are forgotten cases in button definition on GamePage")
                            },
                            fontSize = 20.sp,
                        )
                    }
                is RoundBreakPageComponent.UserRole.SpeakerIn,
                is RoundBreakPageComponent.UserRole.ListenerIn -> {}
            }
        }
    )
}