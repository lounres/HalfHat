package dev.lounres.halfhat.client.common.ui.components.rules

import dev.lounres.halfhat.client.common.ui.components.PageComponent


public interface RulesPageComponent : PageComponent {
    override val textName: String get() = "Rules"
}