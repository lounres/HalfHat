package dev.lounres.halfhat.client.common.ui.components.home

import dev.lounres.halfhat.client.common.ui.components.PageComponent


public interface HomePageComponent : PageComponent {
    override val textName: String get() = "Home"
}