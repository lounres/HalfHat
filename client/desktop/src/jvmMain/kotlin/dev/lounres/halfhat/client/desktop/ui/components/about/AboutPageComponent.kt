package dev.lounres.halfhat.client.desktop.ui.components.about

import dev.lounres.halfhat.client.desktop.ui.components.PageComponent


interface AboutPageComponent : PageComponent {
    override val textName: String get() = "About"
}