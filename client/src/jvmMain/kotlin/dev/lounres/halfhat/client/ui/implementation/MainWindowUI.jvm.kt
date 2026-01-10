package dev.lounres.halfhat.client.ui.implementation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.halfhat.client.ui.components.MainWindowComponent
import dev.lounres.kone.hub.subscribeAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch


@Composable
fun LifecycleController(
    lifecycle: MutableUIComponentLifecycle,
    windowState: WindowState,
    windowInfo: WindowInfo,
) {
    LaunchedEffect(lifecycle, windowState, windowInfo) {
        combine(
            snapshotFlow(windowState::isMinimized),
            snapshotFlow(windowInfo::isWindowFocused),
            ::Pair,
        ).collect { (isMinimized, isFocused) ->
            when {
                isMinimized -> lifecycle.moveTo(UIComponentLifecycleState.Running)
                isFocused -> lifecycle.moveTo(UIComponentLifecycleState.Foreground)
                else -> lifecycle.moveTo(UIComponentLifecycleState.Background)
            }
        }
    }
    
    DisposableEffect(lifecycle) {
        CoroutineScope(Dispatchers.Default).launch {
            lifecycle.moveTo(UIComponentLifecycleState.Running)
        }
        onDispose {
            CoroutineScope(Dispatchers.Default).launch {
                lifecycle.moveTo(UIComponentLifecycleState.Destroyed)
            }
        }
    }
}

@Composable
fun MainWindowUI(
    component: MainWindowComponent?
) {
    if (component != null)
        Window(
            title = "HalfHat — ${component.pageVariants.subscribeAsState().value.active.component.component.textName}",
//            icon = painterResource(Res.drawable.halfhat_logo), // TODO: Add window icon
            state = component.windowState,
            onCloseRequest = component.onWindowCloseRequest,
        ) {
            LifecycleController(
                component.globalLifecycle,
                component.windowState,
                LocalWindowInfo.current,
            )
            
            MainWindowContentUI(
                component = component,
                windowSizeClass = calculateWindowSizeClass()
            )
        }
    else
        Window(
            title = "HalfHat",
//            icon = painterResource(Res.drawable.halfhat_logo), // TODO: Add window icon
            state = rememberWindowState(
                position = WindowPosition.Aligned(Alignment.Center),
                size = DpSize(400.dp, 300.dp)
            ),
            undecorated = true,
            resizable = false,
            onCloseRequest = {},
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Loading...",
                        fontSize = 36.sp,
                    )
                    ContainedLoadingIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
}