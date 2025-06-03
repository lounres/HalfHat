package dev.lounres.halfhat.client.desktop.ui.components.faq

import dev.lounres.halfhat.client.desktop.ui.components.PageComponent


interface FAQPageComponent : PageComponent {
    override val textName: String get() = "FAQ"
    val onFeedbackLinkClick: () -> Unit
}