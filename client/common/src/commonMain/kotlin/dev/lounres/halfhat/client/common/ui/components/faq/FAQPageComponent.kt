package dev.lounres.halfhat.client.common.ui.components.faq

import dev.lounres.halfhat.client.common.ui.components.PageComponent


public interface FAQPageComponent : PageComponent {
    override val textName: String get() = "FAQ"
    public val onFeedbackLinkClick: () -> Unit
}