package dev.lounres.thetruehat.client.common.ui.onlineGame.roomEnter

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
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.lounres.thetruehat.client.common.components.onlineGame.roomEnter.RoomEnterPageComponent
import dev.lounres.thetruehat.client.common.uiTemplates.TheTrueHatPageWithHatTemplateUI


//@Preview
//@Composable
//fun RoomEnterPageUIPreview() {
//    RoomEnterPageUI(
//        component = FakeRoomEnterPageComponent()
//    )
//}

@Composable
public fun RoomEnterPageUI(
    component: RoomEnterPageComponent,
) {
    TheTrueHatPageWithHatTemplateUI(
        backButtonEnabled = component.backButtonEnabled,
        onBackButtonClick = component.onBackButtonClick,
        onLanguageChange = component.onLanguageChange,
        onFeedbackButtonClick = component.onFeedbackButtonClick,
        onHatButtonClick = component.onHatButtonClick,
        pageContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(350.dp)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val roomId by component.roomIdField.subscribeAsState()
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
                    onValueChange = component.onRoomIdChange,
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
                        onClick = component.onRoomIdPaste,
                    ) {
                        Text("Вставить")
                    }
                    Button(
                        shape = CircleShape,
                        onClick = component.onRoomIdGenerate,
                    ) {
                        Text("Сгенерировать новый")
                    }
                }
                Spacer(
                    modifier = Modifier.height(30.dp)
                )
                val nickname by component.nicknameField.subscribeAsState()
                OutlinedTextField(
                    placeholder = {
                        Text("Введи своё имя")
                    },
                    value = nickname,
                    onValueChange = component.onNicknameChange,
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
                    onClick = component.onLetsGoButtonClick,
                ) {
                    Text("Поехали!")
                }
            }
        }
    )
}