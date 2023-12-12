package dev.lounres.thetruehat.client.desktop.ui.game.roomFlow.roomSettings

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
import dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomSettings.FakeRoomSettingsPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomSettings.RoomSettingsPageComponent
import dev.lounres.thetruehat.client.desktop.uiTemplates.TheTrueHatPageWithHatTemplateUI


@Preview
@Composable
fun RoomSettingsPageUIPreview() {
    RoomSettingsPageUI(
        component = FakeRoomSettingsPageComponent(

        )
    )
}

@Composable
fun RoomSettingsPageUI(
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
                    OutlinedButton(
                        shape = CircleShape,
                        onClick = { expanded = !expanded },
                    ) {
                        Text(
                            text = "пока не кончатся слова",
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
                            onClick = { /* TODO */ }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "заданное число кругов",
                                    fontSize = 20.sp,
                                )
                            },
                            onClick = { /* TODO */ }
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
                        enabled = false,
                        shape = CircleShape,
                        onClick = { expanded = !expanded },
                    ) {
                        Text(
                            text = "Русские слова, 14141 слов", // TODO
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
                                    text = "Русские слова, 14141 слов",
                                    fontSize = 20.sp,
                                )
                            },
                            onClick = { /* TODO */ }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "заданное число кругов",
                                    fontSize = 20.sp,
                                )
                            },
                            onClick = { /* TODO */ }
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
                    text = "Количество слов",
                    fontSize = 20.sp,
                )
                TextField(
                    modifier = Modifier.width(90.dp),
                    value = "100", // TODO
                    onValueChange = { /* TODO */ },
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
                    text = "Время на подготовку",
                    fontSize = 20.sp,
                )
                TextField(
                    modifier = Modifier.width(90.dp),
                    value = "3", // TODO
                    onValueChange = { /* TODO */ },
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
                    value = "40", // TODO
                    onValueChange = { /* TODO */ },
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
                    value = "3", // TODO
                    onValueChange = { /* TODO */ },
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
                    checked = false, // TODO
                    onCheckedChange = { /* TODO */ },
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
                   onClick = {},
                ) {
                    Text(
                        text = "Применить",
                        fontSize = 20.sp,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedButton(
                    onClick = {},
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