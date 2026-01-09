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
import dev.lounres.halfhat.client.resources.allFontResources
import dev.lounres.halfhat.client.storage.settings.Settings
import dev.lounres.halfhat.client.ui.components.MainWindowComponent
import dev.lounres.halfhat.client.ui.components.RealMainWindowComponent
import dev.lounres.halfhat.client.ui.components.defaultDarkThemeMode
import dev.lounres.halfhat.client.ui.components.settingsDefaults
import dev.lounres.halfhat.client.ui.components.settingsSerializer
import dev.lounres.halfhat.client.ui.implementation.MainWindowUI
import dev.lounres.halfhat.client.ui.theming.HalfhatTheme
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.halfhat.client.utils.DefaultSounds
import dev.lounres.kone.hub.buildSubscriptionLocking
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.registry.correspondsTo
import kotlinx.browser.document
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.configureWebResources
import org.jetbrains.compose.resources.preloadFont
import web.storage.localStorage


@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun application() {
    configureWebResources {
        resourcePathMapping { path -> "${WebPageSettings.base}$path" }
    }
    
    ComposeViewport(document.body!!) {
        val preloadedFonts = Res.allFontResources.mapValues { (_, resource) -> preloadFont(resource) }
        var soundsAreReady by remember { mutableStateOf(false) }
        var darkTheme by remember { mutableStateOf(defaultDarkThemeMode) }
        var component by remember { mutableStateOf<MainWindowComponent?>(null) }
        
        val allPreloaded by remember {
            derivedStateOf {
                preloadedFonts.all { it.value.value != null } && soundsAreReady && component != null
            }
        }
        
        HalfhatTheme(
            darkTheme = darkTheme,
        ) {
            MainWindowUI(if (allPreloaded) component!! else null)
        }
        
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
                val initialSettings = Settings {
                    localStorage.getItem("settings")?.let { setFrom(Json.decodeFromString(settingsSerializer, it)) }
                    @Suppress("UNCHECKED_CAST")
                    for ((key, value) in settingsDefaults.values) if (key !in this) (key as RegistryKey<Any?>) correspondsTo value
                }
                darkTheme = initialSettings.darkTheme
                component = RealMainWindowComponent(
                    initialSettings = initialSettings,
                ).also { component ->
                    component.darkTheme.buildSubscriptionLocking {
                        darkTheme = it
                        subscribe { darkTheme = it }
                    }
                    component.globalLifecycle.moveTo(UIComponentLifecycleState.Foreground)
                }
            }
        }
    }
}