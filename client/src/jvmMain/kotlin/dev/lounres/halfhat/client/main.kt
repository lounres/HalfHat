package dev.lounres.halfhat.client

import androidx.compose.runtime.*
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import dev.lounres.halfhat.client.storage.settings.Settings
import dev.lounres.halfhat.client.ui.components.MainWindowComponent
import dev.lounres.halfhat.client.ui.components.RealMainWindowComponent
import dev.lounres.halfhat.client.ui.components.defaultDarkThemeMode
import dev.lounres.halfhat.client.ui.components.settingsDefaults
import dev.lounres.halfhat.client.ui.implementation.MainWindowUI
import dev.lounres.halfhat.client.ui.theming.HalfhatTheme
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.registry.correspondsTo


fun main() {
    application(
        exitProcessOnExit = false,
    ) {
        var darkTheme by remember { mutableStateOf(defaultDarkThemeMode) }
        var component by remember { mutableStateOf<MainWindowComponent?>(null) }
        
        HalfhatTheme(
            darkTheme = darkTheme,
        ) {
            MainWindowUI(component)
        }
        
        LaunchedEffect(Unit) {
            val initialSettings = Settings {
                // TODO: Add loading of saved settings
                @Suppress("UNCHECKED_CAST")
                for ((key, value) in settingsDefaults.values) if (key !in this) (key as RegistryKey<Any?>) correspondsTo value
            }
            darkTheme = initialSettings.darkTheme
            component = RealMainWindowComponent(
                initialSettings = initialSettings,
                windowState = WindowState(
//                    placement = WindowPlacement.Maximized,
                ),
                onWindowCloseRequest = ::exitApplication,
            ).also {
                it.darkTheme.subscribe { newTheme -> darkTheme = newTheme }
            }
        }
    }
}