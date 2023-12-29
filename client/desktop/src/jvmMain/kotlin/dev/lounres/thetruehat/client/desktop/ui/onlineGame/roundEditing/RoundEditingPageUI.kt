package dev.lounres.thetruehat.client.desktop.ui.onlineGame.roundEditing

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.update
import dev.lounres.thetruehat.api.models.UserGameState
import dev.lounres.thetruehat.client.common.components.onlineGame.roundEditing.FakeRoundEditingPageComponent
import dev.lounres.thetruehat.client.common.components.onlineGame.roundEditing.RoundEditingPageComponent
import dev.lounres.thetruehat.client.desktop.uiTemplates.RoundPageTemplateUI


@Preview
@Composable
fun RoundEditingPageUIPreview() {
    RoundEditingPageUI(
        component = FakeRoundEditingPageComponent()
    )
}

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
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                val state = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    state = state,
                    horizontalAlignment = Alignment.CenterHorizontally,
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
                                enabled = result.state != UserGameState.WordExplanationResult.State.Explained,
                                onClick = {
                                    it.update {
                                        UserGameState.WordExplanationResult(
                                            word = it.word,
                                            state = UserGameState.WordExplanationResult.State.Explained,
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
                                enabled = result.state != UserGameState.WordExplanationResult.State.NotExplained,
                                onClick = {
                                    it.update {
                                        UserGameState.WordExplanationResult(
                                            word = it.word,
                                            state = UserGameState.WordExplanationResult.State.NotExplained,
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
                                enabled = result.state != UserGameState.WordExplanationResult.State.Mistake,
                                onClick = {
                                    it.update {
                                        UserGameState.WordExplanationResult(
                                            word = it.word,
                                            state = UserGameState.WordExplanationResult.State.Mistake,
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
                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState = state)
                )
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