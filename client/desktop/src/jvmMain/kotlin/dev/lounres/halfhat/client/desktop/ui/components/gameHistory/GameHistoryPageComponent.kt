package dev.lounres.halfhat.client.desktop.ui.components.gameHistory

import dev.lounres.halfhat.client.desktop.ui.components.PageComponent


interface GameHistoryPageComponent : PageComponent {
    override val textName: String get() = "Game history"
}