package dev.lounres.thetruehat.client.desktop.ui.game.roomFlow.roomOverview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomOverview.FakeRoomOverviewPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.roomFlow.roomOverview.RoomOverviewPageComponent
import dev.lounres.thetruehat.client.desktop.uiTemplates.TheTrueHatPageWithHatTemplateUI
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


@Preview
@Composable
fun RoomOverviewPageUIPreview1() {
    RoomOverviewPageUI(
        component = FakeRoomOverviewPageComponent()
    )
}

@Preview
@Composable
fun RoomOverviewPageUIPreview2() {
    RoomOverviewPageUI(
        component = FakeRoomOverviewPageComponent(playerIndex = 2)
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun RoomOverviewPageUI(
    component: RoomOverviewPageComponent,
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
                val playerIndex by component.playerIndex.collectAsState()
                val userListMaybe by component.userList.collectAsState()
                val userList by remember { derivedStateOf { userListMaybe ?: emptyList() } }
                val firstOnline by remember { derivedStateOf { userList.indexOfFirst { it.online } } }
                val isHost by remember { derivedStateOf { playerIndex == firstOnline } }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = component.roomId,
                        fontSize = 30.sp,
                    )
                    if (isHost)
                        IconButton(
                            onClick = component.onSettingsButtonClick
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
                        onClick = component.onRoomIdCopy,
                        shape = CircleShape,
                    ) {
                        Text("Копир. ключ")
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Button(
                        onClick = component.onRoomLinkCopy,
                        shape = CircleShape,
                    ) {
                        Text("Копир. ссылку")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column {
                    Divider()
                    for ((index, user) in userList.withIndex()) if (user.online) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                            if (index == firstOnline)
                                Image(
                                    painterResource("hat.png"),
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.CenterStart).size(20.dp)
                                )
                            Row(
                                modifier = Modifier.align(Alignment.Center),
                            ) {
                                Text(
                                    text = user.username,
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
                if (isHost) // TODO: Move such logic to component.
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        onClick = component.onStartGameButtonClick,
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