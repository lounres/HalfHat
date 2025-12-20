package dev.lounres.halfhat.client.ui.components.faq

import dev.lounres.halfhat.client.ui.components.PageComponent


public interface FAQPageComponent : PageComponent {
    override val textName: String get() = "FAQ"
    public val onFeedbackLinkClick: () -> Unit
}