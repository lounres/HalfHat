package dev.lounres.halfhat.client.ui.components.gameHistory

import dev.lounres.halfhat.client.ui.components.PageComponent


public interface GameHistoryPageComponent : PageComponent {
    override val textName: String get() = "Game history"
}