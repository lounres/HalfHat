package dev.lounres.thetruehat.client.desktop.ui.gameTimer.settings

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.lounres.thetruehat.client.common.components.gameTimer.settings.FakeSettingsPageComponent
import dev.lounres.thetruehat.client.common.components.gameTimer.settings.SettingsPageComponent
import dev.lounres.thetruehat.client.desktop.uiTemplates.TheTrueHatPageWithHatTemplateUI


@Preview
@Composable
fun SettingsPageUIPreview() {
    SettingsPageUI(
        component = FakeSettingsPageComponent()
    )
}

@Composable
fun SettingsPageUI(
    component: SettingsPageComponent
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Время на подготовку",
                    fontSize = 20.sp,
                )
                TextField(
                    modifier = Modifier.width(90.dp),
                    value = component.countdownTime.subscribeAsState().value.toString(),
                    onValueChange = component.onCountdownTimeChange,
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Время на объяснение",
                    fontSize = 20.sp,
                )
                TextField(
                    modifier = Modifier.width(90.dp),
                    value = component.explanationTime.subscribeAsState().value.toString(),
                    onValueChange = component.onExplanationTimeChange,
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Время на последнюю попытку",
                    fontSize = 20.sp,
                )
                TextField(
                    modifier = Modifier.width(90.dp),
                    value = component.finalGuessTime.subscribeAsState().value.toString(),
                    onValueChange = component.onFinalGuessTimeChange,
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
            Spacer(
                modifier = Modifier.height(30.dp)
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = component.onStartButtonClick,
            ) {
                Text(
                    text = "Старт",
                    fontSize = 36.sp,
                )
            }
        }
    }
}