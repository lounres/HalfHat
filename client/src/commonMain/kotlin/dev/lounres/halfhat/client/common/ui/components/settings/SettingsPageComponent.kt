package dev.lounres.halfhat.client.common.ui.components.settings

import dev.lounres.halfhat.client.common.ui.components.PageComponent


public interface SettingsPageComponent : PageComponent {
    override val textName: String get() = "Settings"
}