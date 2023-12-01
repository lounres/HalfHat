package dev.lounres.thetruehat.client.desktop.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.lounres.thetruehat.client.common.ui.CircleButtonWithIcon
import dev.lounres.thetruehat.client.desktop.components.GamePageComponent
import dev.lounres.thetruehat.client.desktop.components.fake.FakeGamePageComponent
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


@Preview
@Composable
fun GamePageUIPreview() {
    GamePageUI(
        component = FakeGamePageComponent(),
        header = {},
        footer = {},
    )
}


@OptIn(ExperimentalResourceApi::class)
@Composable
fun GamePageUI(
    component: GamePageComponent,
    header: @Composable (ColumnScope.() -> Unit),
    footer: @Composable (ColumnScope.() -> Unit),
) {
    TheTrueHatPageWithHatUI(
        component = component.theTrueHatPageWithHatComponent,
        pageContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(350.dp)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val wordsNumber by component.wordsNumber.subscribeAsState()
                Text(
                    text = "$wordsNumber слов",
                    fontSize = 30.sp,
                )
                Row {
                    CircleButtonWithIcon(
                        icon = painterResource("icons/exit_black_x1_24dp.png"),
                        onClick = component::onExitButtonClick,
                    )
                    val volumeOn by component.volumeOn.subscribeAsState()
                    CircleButtonWithIcon(
                        icon = painterResource(if (volumeOn) "icons/volume_on_black_x1_24dp.png" else "icons/volume_off_black_x1_24dp.png"),
                        onClick = component::onVolumeButtonClick
                    )
                    val showFinishButton by component.showFinishButton.subscribeAsState()
                    if (showFinishButton)
                        CircleButtonWithIcon(
                            icon = painterResource("icons/finish_black_x1_24dp.png"),
                            onClick = component::onFinishButtonClick,
                        )
                }
                Spacer(modifier = Modifier.height(10.dp))
                header()
                Spacer(modifier = Modifier.height(30.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    val speakerNickname by component.speakerNickname.subscribeAsState()
                    Text(
                        text = speakerNickname,
                        fontSize = 25.sp,
                        color = Color(15, 170, 74),
                    )
                    Text(
                        text = "объясняет",
                        fontSize = 17.sp,
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "отгадывает",
                        fontSize = 17.sp,
                    )
                    val listenerNickname by component.listenerNickname.subscribeAsState()
                    Text(
                        text = listenerNickname,
                        fontSize = 25.sp,
                        color = Color(0, 140, 255),
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
                footer()
            }
        }
    )
}