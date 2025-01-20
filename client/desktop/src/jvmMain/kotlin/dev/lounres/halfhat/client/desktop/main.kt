package dev.lounres.halfhat.client.desktop

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.remember
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.arkivanov.essenty.statekeeper.saveable
import dev.lounres.halfhat.client.common.utils.readSerializableContainer
import dev.lounres.halfhat.client.desktop.storage.AppDatabase
import dev.lounres.halfhat.client.desktop.storage.DriverFactory
import dev.lounres.halfhat.client.desktop.storage.dictionaries.LocalDictionariesRegistry
import dev.lounres.halfhat.client.desktop.ui.components.RealMainWindowComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.MainWindowUI
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
fun main() {
    val appDatabase = AppDatabase(DriverFactory)
    val localDictionariesRegistry = LocalDictionariesRegistry(appDatabase)
    
    val lifecycle = LifecycleRegistry()
    val stateKeeper = StateKeeperDispatcher()
    
    val componentContext = DefaultComponentContext(lifecycle = lifecycle, stateKeeper = stateKeeper)

    val mainWindowState = WindowState()

    application(
        exitProcessOnExit = false,
    ) {

        LifecycleController(lifecycle, mainWindowState)

        val component = remember {
            RealMainWindowComponent(
                componentContext = componentContext,
                localDictionariesRegistry = localDictionariesRegistry,
                onWindowCloseRequest = ::exitApplication,
            )
        }

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