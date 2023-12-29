package dev.lounres.thetruehat.client.desktop.ui.onlineGame.roomFlow.roomSettings

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.lounres.thetruehat.api.models.Settings
import dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow.roomSettings.FakeRoomSettingsPageComponent
import dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow.roomSettings.RoomSettingsPageComponent
import dev.lounres.thetruehat.client.desktop.uiTemplates.TheTrueHatPageWithHatTemplateUI


@Preview
@Composable
fun RoomSettingsPageUIPreview() {
    RoomSettingsPageUI(
        component = FakeRoomSettingsPageComponent()
    )
}

@Composable
public fun RoomSettingsPageUI(
    component: RoomSettingsPageComponent
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Настройки",
                fontSize = 35.sp,
            )
            Spacer(
                modifier = Modifier.height(30.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Играть",
                    fontSize = 20.sp,
                )
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    val gameEndCondition by component.gameEndCondition.subscribeAsState()
                    OutlinedButton(
                        shape = CircleShape,
                        onClick = { expanded = !expanded },
                    ) {
                        Text(
                            text = when(gameEndCondition) {
                                Settings.GameEndCondition.Words -> "пока не кончатся слова"
                                Settings.GameEndCondition.Rounds -> "заданное число кругов"
                            },
                            fontSize = 20.sp,
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "пока не кончатся слова",
                                    fontSize = 20.sp,
                                )
                            },
                            onClick = {
                                expanded = false
                                component.onWordsGameEndConditionChoice()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "заданное число кругов",
                                    fontSize = 20.sp,
                                )
                            },
                            onClick = {
                                expanded = false
                                component.onRoundsGameEndConditionChoice()
                            }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Словарь",
                    fontSize = 20.sp,
                )
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedButton(
                        shape = CircleShape,
                        onClick = { expanded = !expanded },
                    ) {
                        Text(
                            text = component.dictionary.value.let { "${it.name}, ${it.wordsCount} слов" },
                            fontSize = 20.sp,
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            enabled = false,
                            text = {
                                Text(
                                    text = "От каждого игрока",
                                    fontSize = 20.sp,
                                )
                            },
                            onClick = { TODO() }
                        )
                        DropdownMenuItem(
                            enabled = false,
                            text = {
                                Text(
                                    text = "Загрузить",
                                    fontSize = 20.sp,
                                )
                            },
                            onClick = { TODO() }
                        )
                        for (dictionary in component.availableDictionaries.subscribeAsState().value)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${dictionary.name}, ${dictionary.wordsCount} слов",
                                        fontSize = 20.sp,
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    component.onDictionaryChange(Settings.WordsSource.ServerDictionary(id = dictionary.id))
                                }
                            )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                when(component.gameEndCondition.subscribeAsState().value) {
                    Settings.GameEndCondition.Words -> {
                        Text(
                            text = "Количество слов",
                            fontSize = 20.sp,
                        )
                        TextField(
                            modifier = Modifier.width(90.dp),
                            value = component.wordsCount.subscribeAsState().value.toString(),
                            onValueChange = component.onWordsCountChange,
                            textStyle = TextStyle(
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                            ),
                        )
                    }
                    Settings.GameEndCondition.Rounds -> {
                        Text(
                            text = "Количество кругов",
                            fontSize = 20.sp,
                        )
                        TextField(
                            modifier = Modifier.width(90.dp),
                            value = component.roundsCount.subscribeAsState().value.toString(),
                            onValueChange = component.onRoundsCountChange,
                            textStyle = TextStyle(
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                            ),
                        )
                    }
                }
            }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Checkbox(
                    checked = component.strictMode.subscribeAsState().value,
                    onCheckedChange = component.onStrictModeChange,
                )
                Text(
                    text = "Строгий режим",
                    fontSize = 20.sp,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                   onClick = component.onApplySettingsButtonClick,
                ) {
                    Text(
                        text = "Применить",
                        fontSize = 20.sp,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedButton(
                    onClick = component.onCancelButtonClick,
                ) {
                    Text(
                        text = "Отмена",
                        fontSize = 20.sp,
                    )
                }
            }
        }
    }
}