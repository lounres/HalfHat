package dev.lounres.halfhat.client.desktop

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import dev.lounres.halfhat.client.common.logic.wordsProviders.DeviceGameWordsProviderRegistry
import dev.lounres.halfhat.client.desktop.ui.components.MainWindowComponent
import dev.lounres.halfhat.client.desktop.ui.components.RealMainWindowComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.MainWindowUI


fun main() {
    application(
        exitProcessOnExit = false,
    ) {
        var component by remember { mutableStateOf<MainWindowComponent?>(null) }
        
        MainWindowUI(component)
        
        LaunchedEffect(Unit) {
            component = RealMainWindowComponent(
                deviceGameWordsProviderRegistry = DeviceGameWordsProviderRegistry,
                windowState = WindowState(
//                    placement = WindowPlacement.Maximized,
                    size = DpSize(360.dp, 640.dp), // Mi Note 3
//                    size = DpSize(540.dp, 1200.dp), // POCO X5 Pro 5G
                ),
                onWindowCloseRequest = ::exitApplication,
            )
        }
    }
    
//    application {
//        Window(
//            onCloseRequest = ::exitApplication,
//            state = WindowState(
////                placement = WindowPlacement.Maximized,
//                size = DpSize(360.dp, 640.dp), // Mi Note 3
////                size = DpSize(540.dp, 1200.dp), // POCO X5 Pro 5G
//            ),
//        ) {
//
//            var showContent by remember { mutableStateOf(false) }
//
//            if (showContent) {
//                var selectedItem by remember { mutableIntStateOf(0) }
//                val items = listOf("Home", "Search", "Settings")
//                val state = rememberWideNavigationRailState()
//                val scope = rememberCoroutineScope()
//                val selected = painterResource(Res.drawable.aboutPage_dark_png_24dp)
//                val unselected = painterResource(Res.drawable.deviceGamePlayerIcon_dark_png_24dp)
//
//                Row(Modifier.fillMaxWidth()) {
//                    WideNavigationRail(
//                        state = state,
//                        header = {
//                            IconButton(
//                                modifier =
//                                    Modifier.padding(start = 24.dp).semantics {
//                                        // The button must announce the expanded or collapsed state of the rail
//                                        // for accessibility.
//                                        stateDescription =
//                                            if (state.currentValue == WideNavigationRailValue.Expanded) {
//                                                "Expanded"
//                                            } else {
//                                                "Collapsed"
//                                            }
//                                    },
//                                onClick = {
//                                    scope.launch {
//                                        if (state.targetValue == WideNavigationRailValue.Expanded)
//                                            state.collapse()
//                                        else state.expand()
//                                    }
//                                }
//                            ) {
//                                if (state.targetValue == WideNavigationRailValue.Expanded) {
//                                    Icon(modifier = commonIconModifier, painter = painterResource(Res.drawable.closeMenuButton_dark_png_24dp), contentDescription = "Collapse rail")
//                                } else {
//                                    Icon(modifier = commonIconModifier, painter = painterResource(Res.drawable.openMenuButton_dark), contentDescription = "Expand rail")
//                                }
//                            }
//                        }
//                    ) {
//                        items.forEachIndexed { index, item ->
//                            WideNavigationRailItem(
//                                railExpanded = state.targetValue == WideNavigationRailValue.Expanded,
//                                icon = {
//                                    val imageVector =
//                                        if (selectedItem == index) {
//                                            selected
//                                        } else {
//                                            unselected
//                                        }
//                                    Icon(modifier = commonIconModifier, painter = imageVector, contentDescription = null)
//                                },
//                                label = { Text(item) },
//                                selected = selectedItem == index,
//                                onClick = { selectedItem = index }
//                            )
//                        }
//                    }
//
//                    val textString =
//                        if (state.currentValue == WideNavigationRailValue.Expanded) {
//                            "Expanded"
//                        } else {
//                            "Collapsed"
//                        }
//                    Column {
//                        Text(modifier = Modifier.padding(16.dp), text = "Is animating: " + state.isAnimating)
//                        Text(modifier = Modifier.padding(16.dp), text = "The rail is $textString.")
//                        Text(
//                            modifier = Modifier.padding(16.dp),
//                            text =
//                                "Note: The orientation of this demo has been locked to portrait mode, because" +
//                                        " landscape mode may result in a compact height in certain devices. For" +
//                                        " any compact screen dimensions, use a Navigation Bar instead."
//                        )
//                    }
//                }
//            }
//
//            LaunchedEffect(Unit) {
//                showContent = true
//            }
//        }
//    }
}