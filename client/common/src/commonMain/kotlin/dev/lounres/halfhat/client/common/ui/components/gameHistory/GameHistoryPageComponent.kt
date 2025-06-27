package dev.lounres.halfhat.client.common.ui.components.gameHistory

import dev.lounres.halfhat.client.common.ui.components.PageComponent


public interface GameHistoryPageComponent : PageComponent {
    override val textName: String get() = "Game history"
}