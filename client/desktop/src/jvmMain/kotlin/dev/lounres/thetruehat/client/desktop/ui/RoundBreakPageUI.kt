package dev.lounres.thetruehat.client.desktop.ui

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
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import dev.lounres.thetruehat.client.desktop.components.RoundBreakPageComponent
import dev.lounres.thetruehat.client.desktop.components.RoundBreakUserRole
import dev.lounres.thetruehat.client.desktop.components.fake.FakeRoundBreakPageComponent


@Preview
@Composable
fun RoundBreakPageUIPreview1() {
    RoundBreakPageUI(
        component = FakeRoundBreakPageComponent(
            userRole = RoundBreakUserRole.SpeakerReady
        )
    )
}

@Preview
@Composable
fun RoundBreakPageUIPreview2() {
    RoundBreakPageUI(
        component = FakeRoundBreakPageComponent(
            userRole = RoundBreakUserRole.SpeakerWaiting
        )
    )
}

@Preview
@Composable
fun RoundBreakPageUIPreview3() {
    RoundBreakPageUI(
        component = FakeRoundBreakPageComponent(
            userRole = RoundBreakUserRole.SpeakerIn(3u)
        )
    )
}

@Composable
fun RoundBreakPageUI(
    component: RoundBreakPageComponent,
) {
    GamePageUI(
        component = component.gamePageComponent,
        header = {
            when (val userRole = component.userRole) {
                RoundBreakUserRole.SpeakerWaiting, RoundBreakUserRole.SpeakerReady -> {
                    Text(
                        text = "Ты объясняешь",
                        fontSize = 30.sp,
                    )
                }
                RoundBreakUserRole.ListenerWaiting, RoundBreakUserRole.ListenerReady -> {
                    Text(
                        text = "Ты отгадываешь",
                        fontSize = 30.sp,
                    )
                }
                is RoundBreakUserRole.SpeakerIn -> {
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
                is RoundBreakUserRole.ListenerIn -> {
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
                RoundBreakUserRole.SpeakerWaiting, RoundBreakUserRole.ListenerWaiting, RoundBreakUserRole.SpeakerReady, RoundBreakUserRole.ListenerReady ->
                    Button(
                        enabled = userRole == RoundBreakUserRole.SpeakerWaiting || userRole == RoundBreakUserRole.ListenerWaiting,
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        onClick = {},
                    ) {
                        Text(
                            when (userRole) {
                                RoundBreakUserRole.SpeakerWaiting -> "Я готов объяснять"
                                RoundBreakUserRole.ListenerWaiting -> "Я готов отгадывать"
                                RoundBreakUserRole.SpeakerReady, RoundBreakUserRole.ListenerReady -> "Подожди напарника"
                                else -> error("There are forgotten cases in button definition on GamePage")
                            },
                            fontSize = 20.sp,
                        )
                    }
                else -> {}
            }
        }
    )
}