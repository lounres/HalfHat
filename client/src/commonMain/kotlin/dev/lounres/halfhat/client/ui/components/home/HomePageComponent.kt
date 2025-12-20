package dev.lounres.halfhat.client.ui.components.home

import dev.lounres.halfhat.client.ui.components.PageComponent


public interface HomePageComponent : PageComponent {
    override val textName: String get() = "Home"
}