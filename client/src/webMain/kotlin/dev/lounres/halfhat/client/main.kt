package dev.lounres.halfhat.client

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.halfhat.client.consts.WebPageSettings
import dev.lounres.halfhat.client.resources.Res
import dev.lounres.halfhat.client.resources.allDrawableResources
import dev.lounres.halfhat.client.ui.components.MainWindowComponent
import dev.lounres.halfhat.client.ui.components.RealMainWindowComponent
import dev.lounres.halfhat.client.ui.implementation.MainWindowUI
import dev.lounres.halfhat.client.utils.DefaultSounds
import kotlinx.browser.document
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.configureWebResources
import org.jetbrains.compose.resources.preloadImageVector


@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun application() {
    configureWebResources {
        resourcePathMapping { path -> "${WebPageSettings.base}$path" }
    }
    
    ComposeViewport(document.body!!) {
        val preloadedDrawables =
            Res.allDrawableResources.mapValues { (_, resource) -> preloadImageVector(resource) }
        var soundsAreReady by remember { mutableStateOf(false) }
        var component by remember { mutableStateOf<MainWindowComponent?>(null) }
        
        val allPreloaded by remember { derivedStateOf { preloadedDrawables.all { it.value.value != null } && soundsAreReady && component != null } }
        
        MainWindowUI(if (allPreloaded) component!! else null)
        
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
    }
}