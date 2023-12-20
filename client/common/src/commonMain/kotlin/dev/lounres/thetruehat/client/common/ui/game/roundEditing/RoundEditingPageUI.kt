package dev.lounres.thetruehat.client.common.ui.game.roundEditing

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.update
import dev.lounres.thetruehat.api.models.RoomDescription
import dev.lounres.thetruehat.client.common.components.game.roundEditing.RoundEditingPageComponent
import dev.lounres.thetruehat.client.common.uiTemplates.RoundPageTemplateUI


//@Preview
//@Composable
//fun RoundEditingPageUIPreview() {
//    RoundEditingPageUI(
//        component = FakeRoundEditingPageComponent()
//    )
//}

@Composable
public fun RoundEditingPageUI(
    component: RoundEditingPageComponent
) {
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
            text = "Редактирование раунда",
            fontSize = 30.sp,
        )
        val explanationResults = component.explanationResults
        if (explanationResults != null) {
            // TODO: Make the lazy column scrollable
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(explanationResults) {
                    val result by it.subscribeAsState()
                    Text(
                        text = result.word,
                        fontSize = 25.sp,
                    )
                    Row {
                        // TODO: Fix buttons coloring
                        Button(
                            modifier = Modifier.weight(1f),
                            enabled = result.state != RoomDescription.WordExplanationResult.State.Explained,
                            onClick = {
                                it.update {
                                    RoomDescription.WordExplanationResult(
                                        word = it.word,
                                        state = RoomDescription.WordExplanationResult.State.Explained,
                                    )
                                }
                            },
                        ) {
                            Text(
                                text = "угадал"
                            )
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            enabled = result.state != RoomDescription.WordExplanationResult.State.NotExplained,
                            onClick = {
                                it.update {
                                    RoomDescription.WordExplanationResult(
                                        word = it.word,
                                        state = RoomDescription.WordExplanationResult.State.NotExplained,
                                    )
                                }
                            },
                        ) {
                            Text(
                                text = "не угадал"
                            )
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            enabled = result.state != RoomDescription.WordExplanationResult.State.Mistake,
                            onClick = {
                                it.update {
                                    RoomDescription.WordExplanationResult(
                                        word = it.word,
                                        state = RoomDescription.WordExplanationResult.State.Mistake,
                                    )
                                }
                            },
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
                onClick = component.onSubmitButtonClick,
            ) {
                Text(
                    text = "Подтвердить",
                    fontSize = 20.sp,
                )
            }
        }
    }
}