package dev.lounres.halfhat.client.common.ui.components.feedback

import dev.lounres.halfhat.client.common.ui.components.PageComponent


public interface FeedbackPageComponent : PageComponent {
    override val textName: String get() = "Feedback"
}