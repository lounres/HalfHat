package dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.previewScreen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.enterOnlineRoomButton_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.previewScreen.PreviewScreenComponent
import dev.lounres.kone.collections.iterables.next
import org.jetbrains.compose.resources.painterResource


@Composable
public fun RowScope.PreviewScreenActionsUI(
    component: PreviewScreenComponent,
) {

}

@Composable
public fun ColumnScope.PreviewScreenUI(
    component: PreviewScreenComponent,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = component.currentRoomSearchEntry.collectAsState().value,
            onValueChange = component.onChangeRoomSearchEntry,
            label = {
                Text(
                    text = "Room ID"
                )
            }
        )
        OutlinedButton(
            onClick = component.generateRoomSearchEntry
        ) {
            Text(
                text = "Generate another ID"
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (val roomPreview = component.currentRoomPreview.collectAsState().value) {
                PreviewScreenComponent.RoomPreview.Empty -> {}
                PreviewScreenComponent.RoomPreview.Loading ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                
                is PreviewScreenComponent.RoomPreview.Present ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = roomPreview.info.name,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                            ) {
                                Text(
                                    text = "Players:",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                                    verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                                ) {
                                    for (player in roomPreview.info.playersList)
                                        Box(
                                            modifier = Modifier.border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                        ) {
                                            Text(
                                                modifier = Modifier.padding(8.dp),
                                                text = player.name,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    OutlinedTextField(
                                        modifier = Modifier.weight(1f),
                                        value = component.currentEnterName.collectAsState().value,
                                        onValueChange = { component.currentEnterName.value = it },
                                        label = {
                                            Text(
                                                text = "Your nickname"
                                            )
                                        }
                                    )
                                    OutlinedIconButton(
                                        onClick = component.onJoinRoom
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.enterOnlineRoomButton_dark_png_24dp),
                                            contentDescription = "Join the room",
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}