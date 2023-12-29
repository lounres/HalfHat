package dev.lounres.thetruehat.client.desktop.ui.feedback

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.thetruehat.client.common.components.feedback.FakeFeedbackPageComponent
import dev.lounres.thetruehat.client.common.components.feedback.FeedbackPageComponent
import dev.lounres.thetruehat.client.desktop.uiTemplates.TheTrueHatPageWithHatTemplateUI


@Preview
@Composable
public fun FeedbackPageUIPreview() {
    FeedbackPageUI(
        component = FakeFeedbackPageComponent(),
    )
}

@Composable
public fun FeedbackPageUI(
    component: FeedbackPageComponent
) {
    TheTrueHatPageWithHatTemplateUI(
        backButtonEnabled = component.backButtonEnabled,
        onBackButtonClick = component.onBackButtonClick,
        onLanguageChange = component.onLanguageChange,
        onFeedbackButtonClick = component.onFeedbackButtonClick,
        // TODO: Remove the button from the page
        onHatButtonClick = component.onHatButtonClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(350.dp)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Обратная связь",
                    fontSize = 30.sp,
                )
                Text(
                    text = "v0.0.0",
                    fontSize = 20.sp,
                    color = Color.Gray,
                )
            }
            var feedBack by remember { mutableStateOf("") }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = feedBack,
                onValueChange = { feedBack = it },
                label = { Text("Оставьте свой комментарий здесь") },
                singleLine = false,
                minLines = 10,
                maxLines = 10,
            )
            var sendAdditionalData by remember { mutableStateOf(true) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = sendAdditionalData,
                    onCheckedChange = { sendAdditionalData = it },
                )
                Text(
                    text = "Отправить дополнительные сведения"
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = { component.sendFeedback(feedBack, sendAdditionalData) },
            ) {
                Text("Отправить")
            }
        }
    }
}