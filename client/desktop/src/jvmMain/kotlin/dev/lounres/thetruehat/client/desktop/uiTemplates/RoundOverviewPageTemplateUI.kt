package dev.lounres.thetruehat.client.desktop.uiTemplates

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState


@Preview
@Composable
fun RoundOverviewPageTemplateUIPreview() {
    RoundOverviewPageTemplateUI(
        backButtonEnabled = true,
        unitsUntilEnd = MutableValue(UserGameState.UnitsUntilEnd.Words(100)),
        volumeOn = MutableValue(true),
        showFinishButton = MutableValue(true),
        speakerNickname = MutableValue("Jaguar"),
        listenerNickname = MutableValue("Panther"),
        onBackButtonClick = {},
        onLanguageChange = {},
        onFeedbackButtonClick = {},
        onHatButtonClick = {},
        onExitButtonClick = {},
        onVolumeButtonClick = {},
        onFinishButtonClick = {},
        header = {},
        footer = {},
    )
}

@Composable
public fun RoundOverviewPageTemplateUI(
    backButtonEnabled: Boolean,
    unitsUntilEnd: Value<UserGameState.UnitsUntilEnd>,
    volumeOn: Value<Boolean>,
    showFinishButton: Value<Boolean>,
    speakerNickname: Value<String>,
    listenerNickname: Value<String>,
    onBackButtonClick: () -> Unit,
    onLanguageChange: (Language) -> Unit,
    onFeedbackButtonClick: () -> Unit,
    onHatButtonClick: () -> Unit,
    onExitButtonClick: () -> Unit,
    onVolumeButtonClick: () -> Unit,
    onFinishButtonClick: () -> Unit,
    header: @Composable (ColumnScope.() -> Unit),
    footer: @Composable (ColumnScope.() -> Unit),
) {
    RoundPageTemplateUI(
        backButtonEnabled = backButtonEnabled,
        unitsUntilEnd = unitsUntilEnd,
        volumeOn = volumeOn,
        showFinishButton = showFinishButton,
        onBackButtonClick = onBackButtonClick,
        onLanguageChange = onLanguageChange,
        onFeedbackButtonClick = onFeedbackButtonClick,
        onHatButtonClick = onHatButtonClick,
        onExitButtonClick = onExitButtonClick,
        onVolumeButtonClick = onVolumeButtonClick,
        onFinishButtonClick = onFinishButtonClick,
        pageContent = {
            header()
            Spacer(modifier = Modifier.height(30.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = speakerNickname.subscribeAsState().value,
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
                Text(
                    text = listenerNickname.subscribeAsState().value,
                    fontSize = 25.sp,
                    color = Color(0, 140, 255),
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            footer()
        }
    )
}