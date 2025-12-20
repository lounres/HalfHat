package dev.lounres.halfhat.client.common.ui.components.news

import dev.lounres.halfhat.client.common.ui.components.PageComponent


public interface NewsPageComponent : PageComponent {
    override val textName: String get() = "News"
}