package dev.lounres.thetruehat.client.desktop.ui.deviceGame

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.thetruehat.api.models.Settings
import dev.lounres.thetruehat.client.common.resources.Res
import dev.lounres.thetruehat.client.common.resources.words
import dev.lounres.thetruehat.client.desktop.uiTemplates.TheTrueHatPageWithHatTemplateUI
import org.jetbrains.compose.resources.painterResource


@Preview
@Composable
fun RoomPage() {
    TheTrueHatPageWithHatTemplateUI(
        backButtonEnabled = true,
        onBackButtonClick = { TODO() },
        onLanguageChange = { TODO() },
        onFeedbackButtonClick = { TODO() },
        onHatButtonClick = { TODO() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(450.dp)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Игра на устройстве",
                    fontSize = 30.sp,
                )
                IconButton(
                    onClick = { TODO() }
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                    )
                }
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                val state = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(state = state),
                ) {
                    for(name in listOf("Panther", "Jaguar", "Tiger", "Lion")) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 20.sp,
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    onClick = { TODO() }
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                    )
                                }
                                IconButton(
                                    onClick = { TODO() }
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.words),
                                        contentDescription = null,
                                    )
                                }
                                IconButton(
                                    onClick = { TODO() }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                    )
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        IconButton(
                            onClick = { TODO() }
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                            )
                        }
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState = state)
                )
            }
            HorizontalDivider()
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 50.dp),
                    onClick = { TODO() }
                ) {
                    Text(
                        text = "Начать игру",
                        fontSize = 25.sp,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PlayerSettings() {
    TheTrueHatPageWithHatTemplateUI(
        backButtonEnabled = true,
        onBackButtonClick = { TODO() },
        onLanguageChange = { TODO() },
        onFeedbackButtonClick = { TODO() },
        onHatButtonClick = { TODO() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(450.dp)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Настройки игрока",
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
                    text = "Имя",
                    fontSize = 20.sp,
                )
                Spacer(
                    modifier = Modifier.width(30.dp)
                )
                TextField(
                    modifier = Modifier.weight(1f),
                    value = "Глеб"/*component.countdownTime.subscribeAsState().value.toString()*/,
                    onValueChange = {}/*component.onCountdownTimeChange*/,
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
            Text(
                modifier = Modifier.align(Alignment.Start),
                text = "Слова",
                fontSize = 20.sp,
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = "",
                onValueChange = {},
                textStyle = TextStyle(
                    fontSize = 20.sp
                ),
                minLines = 10,
            )
        }
    }
}

@Preview
@Composable
fun RoomSettings() {
    TheTrueHatPageWithHatTemplateUI(
        backButtonEnabled = true,
        onBackButtonClick = { TODO() },
        onLanguageChange = { TODO() },
        onFeedbackButtonClick = { TODO() },
        onHatButtonClick = { TODO() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(450.dp)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Настройки комнаты",
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
                    val gameEndCondition = Settings.GameEndCondition.Words /*by component.gameEndCondition.subscribeAsState()*/
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
                                /*component.onWordsGameEndConditionChoice()*/
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
                                /*component.onRoundsGameEndConditionChoice()*/
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
                            text = "Словарище, 57 слов" /*component.dictionary.value.let { "${it.name}, ${it.wordsCount} слов" }*/,
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
//                        for (dictionary in component.availableDictionaries.subscribeAsState().value)
//                            DropdownMenuItem(
//                                text = {
//                                    Text(
//                                        text = "${dictionary.name}, ${dictionary.wordsCount} слов",
//                                        fontSize = 20.sp,
//                                    )
//                                },
//                                onClick = {
//                                    expanded = false
//                                    component.onDictionaryChange(Settings.WordsSource.ServerDictionary(id = dictionary.id))
//                                }
//                            )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                when(Settings.GameEndCondition.Words/*component.gameEndCondition.subscribeAsState().value*/) {
                    Settings.GameEndCondition.Words -> {
                        Text(
                            text = "Количество слов",
                            fontSize = 20.sp,
                        )
                        TextField(
                            modifier = Modifier.width(90.dp),
                            value = "57" /*component.wordsCount.subscribeAsState().value.toString()*/,
                            onValueChange = {}/*component.onWordsCountChange*/,
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
                            value = "179"/*component.roundsCount.subscribeAsState().value.toString()*/,
                            onValueChange = {}/*component.onRoundsCountChange*/,
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
                    value = "3"/*component.countdownTime.subscribeAsState().value.toString()*/,
                    onValueChange = {}/*component.onCountdownTimeChange*/,
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
                    value = "40"/*component.explanationTime.subscribeAsState().value.toString()*/,
                    onValueChange = {}/*component.onExplanationTimeChange*/,
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
                    value = "3"/*component.finalGuessTime.subscribeAsState().value.toString()*/,
                    onValueChange = {}/*component.onFinalGuessTimeChange*/,
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
                    checked = false/*component.strictMode.subscribeAsState().value*/,
                    onCheckedChange = {}/*component.onStrictModeChange*/,
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
                    onClick = {}/*component.onApplySettingsButtonClick*/,
                ) {
                    Text(
                        text = "Применить",
                        fontSize = 20.sp,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedButton(
                    onClick = {}/*component.onCancelButtonClick*/,
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