package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roomGathering

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roomGathering.RoomGatheringComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameCopyKeyButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameCopyLinkButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameExitRoomButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameHostMarkIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGamePlayerIcon
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.utils.withIndex


fun RoomGatheringToolbarContentUI(
    component: RoomGatheringComponent,
): @Composable RowScope.() -> Unit = {
    IconButton(
        onClick = component.onExitOnlineGame
    ) {
        Icon(
            modifier = commonIconModifier,
            imageVector = HalfHatIcon.OnlineGameExitRoomButton,
            contentDescription = "Exit online game room"
        )
    }
    IconButton(
        onClick = component.onCopyOnlineGameKey
    ) {
        Icon(
            modifier = commonIconModifier,
            imageVector = HalfHatIcon.OnlineGameCopyKeyButton,
            contentDescription = "Copy online game room key"
        )
    }
    IconButton(
        onClick = component.onCopyOnlineGameLink
    ) {
        Icon(
            modifier = commonIconModifier,
            imageVector = HalfHatIcon.OnlineGameCopyLinkButton,
            contentDescription = "Copy online game room link"
        )
    }
}

val toolbarColors @Composable get() = FloatingToolbarDefaults.vibrantFloatingToolbarColors()

@Composable
fun RoomGatheringRoomCardUI(
    component: RoomGatheringComponent,
    modifier: Modifier,
) {
    val gameState = component.gameState.collectAsState().value
    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val playersList = gameState.playersList
            for ((val index, val player = value) in playersList.withIndex()) {
                if (index != 0u) Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = CircleShape,
                    color =
                        if (index == gameState.selfRole.userIndex) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (player.isHost)
                            Icon(
                                imageVector = HalfHatIcon.OnlineGameHostMarkIcon,
                                modifier = Modifier.size(24.dp),
                                contentDescription = null,
                            )
                        else
                            Spacer(Modifier.width(24.dp))
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = HalfHatIcon.OnlineGamePlayerIcon,
                            modifier = Modifier.size(24.dp),
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = player.name)
                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.RoomGatheringCompactUI(
    component: RoomGatheringComponent,
) {
    HorizontalFloatingToolbar(
        colors = toolbarColors,
        expanded = true,
        content = RoomGatheringToolbarContentUI(component),
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    val cardModifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().weight(1f)
    RoomGatheringRoomCardUI(component, cardModifier)
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(
        enabled = component.gameState.collectAsState().value.selfRole.isRoomFixable,
        onClick = component.onFixRoom
    ) {
        Text("FIX ROOM", fontSize = 32.sp)
    }
}

@Composable
fun ColumnScope.RoomGatheringLargeUI(
    component: RoomGatheringComponent,
) {
    HorizontalFloatingToolbar(
        colors = toolbarColors,
        expanded = true,
        content = RoomGatheringToolbarContentUI(component),
    )
    
    Spacer(modifier = Modifier.height(16.dp))

    val cardModifier = Modifier.fillMaxHeight().weight(1f, false).widthIn(max = 420.dp)
    RoomGatheringRoomCardUI(component, cardModifier)
    
    Spacer(modifier = Modifier.height(16.dp))

    Button(
        enabled = component.gameState.collectAsState().value.selfRole.isRoomFixable,
        onClick = component.onFixRoom
    ) {
        Text("FIX ROOM", fontSize = 32.sp)
    }
}

@Composable
public fun RoomGatheringUI(
    component: RoomGatheringComponent,
    windowSizeClass: WindowSizeClass,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val gameState = component.gameState.collectAsState().value
        Text(
            text = gameState.roomName,
            fontSize = 48.sp,
        )
        val minWidthDp = windowSizeClass.minWidthDp
        when {
            minWidthDp >= WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND -> RoomGatheringLargeUI(component) // Extra-large width
            minWidthDp >= WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND -> RoomGatheringLargeUI(component) // Large width
            minWidthDp >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND -> RoomGatheringCompactUI(component) // Expanded width
            minWidthDp >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND -> RoomGatheringCompactUI(component) // Medium width
            else -> RoomGatheringCompactUI(component) // Compact width
        }
    }
}