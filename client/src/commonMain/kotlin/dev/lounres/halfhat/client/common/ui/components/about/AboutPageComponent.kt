package dev.lounres.halfhat.client.common.ui.components.about

import dev.lounres.halfhat.client.common.ui.components.PageComponent


public interface AboutPageComponent : PageComponent {
    override val textName: String get() = "About"
}