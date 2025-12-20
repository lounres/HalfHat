package dev.lounres.halfhat.client.ui.components.feedback

import dev.lounres.halfhat.client.ui.components.PageComponent


public interface FeedbackPageComponent : PageComponent {
    override val textName: String get() = "Feedback"
}