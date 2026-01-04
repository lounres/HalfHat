package dev.lounres.halfhat.client

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import dev.lounres.halfhat.client.resources.Res
import dev.lounres.halfhat.client.resources.allDrawableResources
import dev.lounres.halfhat.client.utils.DefaultSounds
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.halfhat.client.consts.WebPageSettings
import dev.lounres.halfhat.client.ui.components.MainWindowComponent
import dev.lounres.halfhat.client.ui.components.RealMainWindowComponent
import dev.lounres.halfhat.client.ui.implementation.MainWindowContentUI
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.client.ui.theming.HalfhatTheme
import kotlinx.browser.document
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.configureWebResources
import org.jetbrains.compose.resources.preloadImageVector


@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
    configureWebResources {
        // Overrides the resource location
        resourcePathMapping { path -> "${WebPageSettings.base}$path" }
    }
    
    ComposeViewport(document.body!!) {
        val preloadedDrawables = Res.allDrawableResources.mapValues { (_, resource) -> preloadImageVector(resource) }
        var soundsAreReady by remember { mutableStateOf(false) }
        var component by remember { mutableStateOf<MainWindowComponent?>(null) }
        
        val allPreloaded by remember { derivedStateOf { preloadedDrawables.all { it.value.value != null } && soundsAreReady && component != null } }
        
        LaunchedEffect(Unit) {
            launch {
                listOf(
                    DefaultSounds.preparationCountdown,
                    DefaultSounds.explanationStart,
                    DefaultSounds.finalGuessStart,
                    DefaultSounds.finalGuessEnd,
                ).joinAll()
                soundsAreReady = true
            }
            launch {
                component = RealMainWindowComponent().also {
                    it.globalLifecycle.moveTo(UIComponentLifecycleState.Foreground)
                }
            }
        }
        
        if (allPreloaded)
            HalfhatTheme(
                darkTheme = DarkTheme.Disabled,
            ) {
                MainWindowContentUI(
                    component = component!!,
                    windowSizeClass = calculateWindowSizeClass()
                )
            }
        else
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
                    ContainedLoadingIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
    }
}