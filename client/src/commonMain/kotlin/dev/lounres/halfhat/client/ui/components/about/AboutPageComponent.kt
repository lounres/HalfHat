package dev.lounres.halfhat.client.ui.components.about

import dev.lounres.halfhat.client.ui.components.PageComponent


public interface AboutPageComponent : PageComponent {
    override val textName: String get() = "About"
}