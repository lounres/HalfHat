package dev.lounres.thetruehat.client.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.lounres.thetruehat.client.common.components.RealRootComponent
import dev.lounres.thetruehat.client.common.ui.RootUI
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalDecomposeApi::class, ExperimentalResourceApi::class)
fun main() {
    val lifecycle = LifecycleRegistry()
    val rootComponentContext: ComponentContext = DefaultComponentContext(lifecycle = lifecycle)
    val rootComponent = RealRootComponent(rootComponentContext)

    application {
        val windowState = rememberWindowState()

        LifecycleController(lifecycle, windowState)

        Window(
            title = "TheTrueHat", // TODO: Replace it with custom (navigation-dependent) title
            icon = painterResource("hat.png"),
            onCloseRequest = ::exitApplication,
        ) {
            RootUI(rootComponent)
        }
    }
}