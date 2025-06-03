package dev.lounres.halfhat.client.desktop.ui.components.home

import dev.lounres.halfhat.client.desktop.ui.components.PageComponent


interface HomePageComponent : PageComponent {
    override val textName: String get() = "Home"
}