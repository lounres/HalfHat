package dev.lounres.halfhat.client.desktop.ui.components.news

import dev.lounres.halfhat.client.desktop.ui.components.PageComponent


interface NewsPageComponent : PageComponent {
    override val textName: String get() = "News"
}