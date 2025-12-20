package dev.lounres.halfhat.client.ui.components.news

import dev.lounres.halfhat.client.ui.components.PageComponent


public interface NewsPageComponent : PageComponent {
    override val textName: String get() = "News"
}