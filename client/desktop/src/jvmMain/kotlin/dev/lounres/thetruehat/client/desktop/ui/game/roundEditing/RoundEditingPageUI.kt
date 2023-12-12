package dev.lounres.thetruehat.client.desktop.ui.game.roundEditing

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import dev.lounres.thetruehat.api.models.RoomDescription
import dev.lounres.thetruehat.client.desktop.components.game.roundEditing.FakeRoundEditingPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.roundEditing.RoundEditingPageComponent
import dev.lounres.thetruehat.client.desktop.uiTemplates.RoundPageTemplateUI


@Preview
@Composable
fun RoundEditingPageUIPreview() {
    RoundEditingPageUI(
        component = FakeRoundEditingPageComponent()
    )
}

@Composable
fun RoundEditingPageUI(
    component: RoundEditingPageComponent
) {
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
            text = "Редактирование раунда",
            fontSize = 30.sp,
        )
        // TODO: Make the lazy column scrollable
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(component.explanationResults) {
                Text(
                    text = it.word,
                    fontSize = 25.sp,
                )
                Row {
                    Button(
                        modifier = Modifier.weight(0.33333334f),
                        onClick = { it.state = RoomDescription.WordExplanationResult.State.Explained },
                    ) {
                        Text(
                            text = "угадал"
                        )
                    }
                    Button(
                        modifier = Modifier.weight(0.33333334f),
                        onClick = { it.state = RoomDescription.WordExplanationResult.State.NotExplained },
                    ) {
                        Text(
                            text = "не угадал"
                        )
                    }
                    Button(
                        modifier = Modifier.weight(0.33333334f),
                        onClick = { it.state = RoomDescription.WordExplanationResult.State.Mistake },
                    ) {
                        Text(
                            text = "ошибка"
                        )
                    }
                }
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {},
        ) {
            Text(
                text = "Подтверждаю",
                fontSize = 20.sp,
            )
        }
    }
}