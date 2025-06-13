package dev.lounres.halfhat.client.desktop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import dev.lounres.halfhat.client.desktop.resources.Res
import dev.lounres.halfhat.client.desktop.resources.aboutPage_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.closeMenuButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.deviceGamePlayerIcon_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.openMenuButton_dark
import dev.lounres.halfhat.client.desktop.storage.AppDatabase
import dev.lounres.halfhat.client.desktop.storage.DriverFactory
import dev.lounres.halfhat.client.desktop.storage.dictionaries.LocalDictionariesRegistry
import dev.lounres.halfhat.client.desktop.ui.components.RealMainWindowComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.MainWindowUI
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource


fun main() {
    val appDatabase = AppDatabase(DriverFactory)
    val localDictionariesRegistry = LocalDictionariesRegistry(appDatabase)

    application(
        exitProcessOnExit = false,
    ) {
        val component = remember {
            RealMainWindowComponent(
                localDictionariesRegistry = localDictionariesRegistry,
                windowState = WindowState(
//                    placement = WindowPlacement.Maximized,
                    size = DpSize(360.dp, 640.dp), // Mi Note 3
//                    size = DpSize(540.dp, 1200.dp), // POCO X5 Pro 5G
                ),
                onWindowCloseRequest = ::exitApplication,
            )
        }

        MainWindowUI(component)
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