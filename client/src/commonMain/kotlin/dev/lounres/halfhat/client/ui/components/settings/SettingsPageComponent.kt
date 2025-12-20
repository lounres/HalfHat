package dev.lounres.halfhat.client.ui.components.settings

import dev.lounres.halfhat.client.ui.components.PageComponent


public interface SettingsPageComponent : PageComponent {
    override val textName: String get() = "Settings"
}