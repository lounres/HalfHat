package dev.lounres.halfhat.client.ui.implementation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.ui.components.MainWindowComponent
import dev.lounres.halfhat.client.ui.theming.HalfhatTheme
import dev.lounres.kone.hub.subscribeAsState


//@Composable
//fun LifecycleController(
//    lifecycle: MutableUIComponentLifecycle,
//    windowState: WindowState,
//    windowInfo: WindowInfo,
//) {
//    LaunchedEffect(lifecycle, windowState, windowInfo) {
//        combine(
//            snapshotFlow(windowState::isMinimized),
//            snapshotFlow(windowInfo::isWindowFocused),
//            ::Pair,
//        ).collect { (isMinimized, isFocused) ->
//            when {
//                isMinimized -> lifecycle.moveTo(UIComponentLifecycleState.Running)
//                isFocused -> lifecycle.moveTo(UIComponentLifecycleState.Foreground)
//                else -> lifecycle.moveTo(UIComponentLifecycleState.Background)
//            }
//        }
//    }
//
//    DisposableEffect(lifecycle) {
//        lifecycle.move(UIComponentLifecycleTransition.Run)
//        onDispose {
//            lifecycle.move(UIComponentLifecycleTransition.Destroy)
//        }
//    }
//}

@Composable
fun MainWindowUI(
    component: MainWindowComponent?
) {
    if (component != null)
        HalfhatTheme(
            darkTheme = component.darkTheme.subscribeAsState().value,
        ) {
            MainWindowContentUI(
                component = component,
                windowSizeClass = calculateWindowSizeClass()
            )
        }
    else
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