package dev.lounres.halfhat.client.ui.implementation.miscellanea.rules

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.ui.components.miscellanea.rules.RulesPageComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.RulesPageIcon
import dev.lounres.halfhat.client.ui.utils.commonIconModifier


@Composable
public fun RulesPageIcon(
    isSelected: Boolean,
) {
    Icon(
        imageVector = HalfHatIcon.RulesPageIcon,
        modifier = commonIconModifier,
        contentDescription = "Rules page",
    )
}

@Composable
public fun RulesPageBadge(
    component: dev.lounres.halfhat.client.ui.components.miscellanea.rules.RulesPageComponent,
    isSelected: Boolean,
) {

}