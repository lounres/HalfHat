package dev.lounres.thetruehat.client.desktop.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.lounres.thetruehat.client.desktop.components.RoomEnterPageComponent
import dev.lounres.thetruehat.client.desktop.components.fake.FakeRoomEnterPageComponent


@Preview
@Composable
fun RoomEnterPageUIPreview() {
    RoomEnterPageUI(
        component = FakeRoomEnterPageComponent()
    )
}

@Composable
fun RoomEnterPageUI(
    component: RoomEnterPageComponent,
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
                val roomId by component.roomId.subscribeAsState()
                val roomIdTextStyle = TextStyle(fontSize = 38.sp, textAlign = TextAlign.Center)
                OutlinedTextField(
                    placeholder = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text("КЛЮЧ ИГРЫ", style = roomIdTextStyle)
                        }
                    },
                    value = roomId,
                    onValueChange = component::onRoomIdChange,
                    textStyle = roomIdTextStyle,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Введённый выше ключ необходим для игры.",
                    modifier = Modifier.padding(10.dp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        shape = CircleShape,
                        onClick = {},
                    ) {
                        Text("Вставить")
                    }
                    Button(
                        shape = CircleShape,
                        onClick = {},
                    ) {
                        Text("Сгенерировать новый")
                    }
                }
                Spacer(
                    modifier = Modifier.height(30.dp)
                )
                val nickname by component.nickname.subscribeAsState()
                OutlinedTextField(
                    placeholder = {
                        Text("Введи своё имя")
                    },
                    value = nickname,
                    onValueChange = component::onNicknameChange,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Имя нужно, чтобы друзья могли тебя опознать.",
                    modifier = Modifier.padding(10.dp),
                )
                Spacer(
                    modifier = Modifier.height(10.dp)
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    onClick = {},
                ) {
                    Text("Поехали!")
                }
            }
        }
    )
}