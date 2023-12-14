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
import dev.lounres.thetruehat.client.desktop.ui.RootUI
import kotlinx.coroutines.runBlocking
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream


@OptIn(ExperimentalDecomposeApi::class)
fun main(): Unit = runBlocking {
    System.setOut(PrintStream(FileOutputStream(FileDescriptor.out), true, "UTF-8"))
    val lifecycle = LifecycleRegistry()
    val rootComponentContext: ComponentContext = DefaultComponentContext(lifecycle = lifecycle)
    val rootComponent = RealRootComponent(rootComponentContext)

    application {
        val windowState = rememberWindowState()

        LifecycleController(lifecycle, windowState)

        Window(
            onCloseRequest = ::exitApplication
        ) {
            RootUI(rootComponent)
        }
    }
}