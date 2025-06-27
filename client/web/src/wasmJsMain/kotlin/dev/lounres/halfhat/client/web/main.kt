package dev.lounres.halfhat.client.web

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.allDrawableResources
import dev.lounres.halfhat.client.web.ui.components.RealMainWindowComponent
import dev.lounres.halfhat.client.web.ui.implementation.MainWindowContentUI
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleTransition
import dev.lounres.komponentual.lifecycle.moveTo
import kotlinx.browser.document
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.configureWebResources
import org.jetbrains.compose.resources.preloadImageVector


@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
    configureWebResources {
        // Overrides the resource location
        resourcePathMapping { path -> "./$path" }
    }
    
    val component = RealMainWindowComponent()
    component.globalLifecycle.moveTo(UIComponentLifecycleState.Foreground)
    ComposeViewport(document.body!!) {
        val preloadedDrawables = Res.allDrawableResources.mapValues { (_, resource) -> preloadImageVector(resource) }
        
        val allPreloaded by remember { derivedStateOf { preloadedDrawables.all { it.value.value != null } } }
        
        LaunchedEffect(allPreloaded) {
            component.globalLifecycle.move(UIComponentLifecycleTransition.Run)
        }
        
        if (allPreloaded) {
            MainWindowContentUI(
                component = component,
                windowSizeClass = calculateWindowSizeClass()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Loading...",
                        fontSize = 36.sp,
                    )
                    LoadingIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}