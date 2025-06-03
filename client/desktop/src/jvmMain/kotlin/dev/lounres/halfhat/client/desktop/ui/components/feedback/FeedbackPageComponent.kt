package dev.lounres.halfhat.client.desktop.ui.components.feedback

import dev.lounres.halfhat.client.desktop.ui.components.PageComponent


interface FeedbackPageComponent : PageComponent {
    override val textName: String get() = "Feedback"
}