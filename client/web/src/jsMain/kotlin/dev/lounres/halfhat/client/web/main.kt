package dev.lounres.halfhat.client.web

import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.lounres.halfhat.client.web.ui.components.RealMainWindowComponent
import dev.lounres.halfhat.client.web.ui.implementation.MainWindowContentUI
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.lifecycle.moveTo
import kotlinx.browser.document


@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val component = RealMainWindowComponent()
    component.globalLifecycle.moveTo(UIComponentLifecycleState.Foreground)
    ComposeViewport(document.body!!) {
        MainWindowContentUI(
            component = component,
            windowSizeClass = calculateWindowSizeClass()
        )
    }
}