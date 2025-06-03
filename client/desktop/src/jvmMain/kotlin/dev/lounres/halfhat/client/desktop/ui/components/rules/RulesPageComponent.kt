package dev.lounres.halfhat.client.desktop.ui.components.rules

import dev.lounres.halfhat.client.desktop.ui.components.PageComponent


interface RulesPageComponent : PageComponent {
    override val textName: String get() = "Rules"
}