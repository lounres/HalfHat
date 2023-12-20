package dev.lounres.thetruehat.client.web

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.CanvasBasedWindow
import androidx.compose.ui.window.Window
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import dev.lounres.thetruehat.client.common.components.RealRootComponent
import dev.lounres.thetruehat.client.common.ui.RootUI
import org.jetbrains.skiko.wasm.onWasmReady
import web.dom.DocumentVisibilityState
import web.dom.document
import web.events.EventType
import web.navigator.navigator


fun main() {
    val lifecycle = LifecycleRegistry()

    val root = RealRootComponent(componentContext = DefaultComponentContext(lifecycle = lifecycle))

    lifecycle.attachToDocument()

    onWasmReady {
        Window("Decompose Sample") {
            RootUI(component = root)
        }
    }
}

private fun LifecycleRegistry.attachToDocument() {
    fun onVisibilityChanged() {
        if (document.visibilityState == DocumentVisibilityState.visible) {
            resume()
        } else {
            stop()
        }
    }

    onVisibilityChanged()

    document.addEventListener(type = EventType("visibilitychange"), callback = { onVisibilityChanged() })
}