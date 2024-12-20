package dev.lounres.halfhat.client.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.lounres.halfhat.client.common.components.RealRootComponent
import dev.lounres.halfhat.client.common.ui.RootUI


@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val lifecycle = LifecycleRegistry()

    val root = RealRootComponent(componentContext = DefaultComponentContext(lifecycle = lifecycle))

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        RootUI(component = root)
    }
}