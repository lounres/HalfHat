package dev.lounres.halfhat.client.desktop.ui.components.settings

import dev.lounres.halfhat.client.desktop.ui.components.PageComponent


interface SettingsPageComponent : PageComponent {
    override val textName: String get() = "Settings"
}