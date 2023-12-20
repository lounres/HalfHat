package dev.lounres.thetruehat.client.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.lounres.thetruehat.client.common.components.RealRootComponent
import dev.lounres.thetruehat.client.common.ui.RootUI


@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val lifecycle = LifecycleRegistry()

    val root = RealRootComponent(componentContext = DefaultComponentContext(lifecycle = lifecycle))

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        RootUI(component = root)
    }
}