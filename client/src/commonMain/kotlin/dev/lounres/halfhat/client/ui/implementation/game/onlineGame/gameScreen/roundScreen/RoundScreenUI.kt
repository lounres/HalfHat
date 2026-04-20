package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.RoundScreenComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameOpenAdditionalCardButton
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.kone.hub.set
import dev.lounres.kone.hub.subscribeAsState
import kotlinx.coroutines.launch


val toolbarColors @Composable get() = FloatingToolbarDefaults.vibrantFloatingToolbarColors()

@Composable
fun ColumnScope.RoundScreenCompactUI(
    component: RoundScreenComponent,
) {
    val openAdditionalCard = component.openAdditionalCard.subscribeAsState().value
    HorizontalFloatingToolbar(
        colors = toolbarColors,
        expanded = true,
        floatingActionButton = {
            ToggleFloatingActionButton(
                checked = openAdditionalCard,
                onCheckedChange = {
                    component.coroutineScope.launch {
                        component.openAdditionalCard.set(it)
                    }
                },
            ) {
                val contentColor = lerp(
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.onPrimary,
                    checkedProgress,
                )
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    Icon(
                        modifier = commonIconModifier,
                        imageVector = HalfHatIcon.OnlineGameOpenAdditionalCardButton,
                        contentDescription = if (openAdditionalCard) "Close settings" else "Open settings"
                    )
                }
            }
        },
        content = RoundScreenToolbarContentUI(component),
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    val cardModifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().weight(1f)
    if (openAdditionalCard) RoundScreenAdditionalCardUI(component, cardModifier)
    else RoundScreenGameCardUI(component, cardModifier)
}

@Composable
fun ColumnScope.RoundScreenLargeUI(
    component: RoundScreenComponent,
) {
    val openAdditionalCard = component.openAdditionalCard.subscribeAsState().value
    HorizontalFloatingToolbar(
        colors = toolbarColors,
        expanded = true,
        floatingActionButton = {
            ToggleFloatingActionButton(
                checked = openAdditionalCard,
                onCheckedChange = {
                    component.coroutineScope.launch {
                        component.openAdditionalCard.set(it)
                    }
                },
            ) {
                val contentColor = lerp(
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.onPrimary,
                    checkedProgress,
                )
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    Icon(
                        modifier = commonIconModifier,
                        imageVector = HalfHatIcon.OnlineGameOpenAdditionalCardButton,
                        contentDescription = if (openAdditionalCard) "Close settings" else "Open settings"
                    )
                }
            }
        },
        content = RoundScreenToolbarContentUI(component),
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalArrangement = Arrangement.Center,
    ) {
        val cardModifier = Modifier.fillMaxHeight().weight(1f, false).widthIn(max = 420.dp)
        RoundScreenGameCardUI(component, cardModifier)
        if (openAdditionalCard) {
            Spacer(modifier = Modifier.width(32.dp))
            RoundScreenAdditionalCardUI(component, cardModifier)
        }
    }
}

@Composable
fun RoundScreenUI(
    component: RoundScreenComponent,
    windowSizeClass: WindowSizeClass,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = component.gameState.collectAsState().value.roomName,
            fontSize = 48.sp,
        )
        val minWidthDp = windowSizeClass.minWidthDp
        when {
            minWidthDp >= WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND -> RoundScreenLargeUI(component) // Extra-large width
            minWidthDp >= WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND -> RoundScreenLargeUI(component) // Large width
            minWidthDp >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND -> RoundScreenCompactUI(component) // Expanded width
            minWidthDp >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND -> RoundScreenCompactUI(component) // Medium width
            else -> RoundScreenCompactUI(component) // Compact width
        }
    }
}