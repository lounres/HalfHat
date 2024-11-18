package dev.lounres.halfhat.client.desktop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.lounres.halfhat.client.desktop.resources.Res
import dev.lounres.halfhat.client.desktop.resources.deviceGamePlayerIcon_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.onlineGameHostMark_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.RealMainWindowComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.MainWindowUI
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
fun main() {
    val lifecycle = LifecycleRegistry()

    val componentContext = DefaultComponentContext(lifecycle = lifecycle)

    application(
        exitProcessOnExit = false,
    ) {
        val windowState = rememberWindowState()

        LifecycleController(lifecycle, windowState)

        val component = RealMainWindowComponent(
            componentContext = componentContext,
            onWindowCloseRequest = ::exitApplication,
        )

        MainWindowUI(component)
    }
    
//    application {
//        Window(
//            state = rememberWindowState(
//                size = DpSize(360.dp, 640.dp), // Mi Note 3
////            size = DpSize(540.dp, 1200.dp), // POCO X5 Pro 5G
//            ),
//            onCloseRequest = ::exitApplication,
//        ) {
//            Scaffold(
//                topBar = {
//                    CenterAlignedTopAppBar(
//                        title = { Text(text = "НОЦУЛУРИРЕД") },
//                    )
//                },
//                bottomBar = {
//                    BottomAppBar {}
//                }
//            ) {
//                Column(
//                    modifier = Modifier.padding(it).fillMaxSize(),
//                ) {
//                    Column(
//                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
//                    ) {
//                        Row(
//                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
//                            verticalAlignment = Alignment.CenterVertically,
//                        ) {
//                            Icon(
//                                painter = painterResource(Res.drawable.onlineGameHostMark_dark_png_24dp),
//                                modifier = Modifier.size(24.dp),
//                                contentDescription = null,
//                            )
//                            Spacer(Modifier.width(4.dp))
//                            Icon(
//                                painter = painterResource(Res.drawable.deviceGamePlayerIcon_dark_png_24dp),
//                                modifier = Modifier.size(24.dp),
//                                contentDescription = null,
//                            )
//                            Spacer(modifier = Modifier.width(16.dp))
//                            Text(
//                                text = "Полина"
//                            )
//                        }
//                        Row(
//                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
//                            verticalAlignment = Alignment.CenterVertically,
//                        ) {
//                            Spacer(modifier = Modifier.width(24.dp))
//                            Spacer(Modifier.width(4.dp))
//                            Icon(
//                                painter = painterResource(Res.drawable.deviceGamePlayerIcon_dark_png_24dp),
//                                modifier = Modifier.size(24.dp),
//                                contentDescription = null,
//                            )
//                            Spacer(modifier = Modifier.width(16.dp))
//                            Text(
//                                text = "Глеб"
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
}