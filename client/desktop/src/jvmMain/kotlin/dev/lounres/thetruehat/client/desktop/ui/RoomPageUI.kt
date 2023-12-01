package dev.lounres.thetruehat.client.desktop.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import dev.lounres.thetruehat.client.desktop.components.RoomPageComponent
import dev.lounres.thetruehat.client.desktop.components.fake.FakeRoomPageComponent
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


@Preview
@Composable
fun RoomPageUIPreview1() {
    RoomPageUI(
        component = FakeRoomPageComponent()
    )
}

@Preview
@Composable
fun RoomPageUIPreview2() {
    RoomPageUI(
        component = FakeRoomPageComponent(playerIndex = 2)
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun RoomPageUI(
    component: RoomPageComponent,
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
                val playerIndex by component.playerIndex.subscribeAsState()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = component.roomId,
                        fontSize = 30.sp,
                    )
                    IconButton(
                        onClick = {  }
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { },
                        shape = CircleShape,
                    ) {
                        Text("Копир. ключ")
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Button(
                        onClick = { },
                        shape = CircleShape,
                    ) {
                        Text("Копир. ссылку")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column {
                    Divider()
                    val userList by component.userList.subscribeAsState()
                    for ((index, user) in userList.withIndex()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                            if (index == 0)
                                Image(
                                    painterResource("hat.png"),
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.CenterStart).size(20.dp)
                                )
                            Row(
                                modifier = Modifier.align(Alignment.Center),
                            ) {
                                Text(
                                    text = user,
                                )
                                if (index == playerIndex)
                                    Text(
                                        text = " (ты)",
                                        fontWeight = FontWeight.Bold
                                    )
                            }
                        }
                        Divider()
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                if (playerIndex == 0)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        onClick = {},
                    ) {
                        Text(
                            "Начать игру",
                            fontSize = 25.sp,
                        )
                    }
                else
                    Text(
                        text = "Игра ещё не началась",
                        fontSize = 25.sp,
                    )
            }
        }
    )
}