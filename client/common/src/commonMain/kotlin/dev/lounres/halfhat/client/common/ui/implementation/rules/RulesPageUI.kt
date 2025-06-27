package dev.lounres.halfhat.client.common.ui.implementation.rules

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.rulesPage_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.rules.RulesPageComponent
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
public fun RulesPageIcon(
    isSelected: Boolean,
) {
    Icon(
        painter = painterResource(Res.drawable.rulesPage_dark_png_24dp),
        modifier = commonIconModifier,
        contentDescription = "Rules page",
    )
}

@Composable
public fun RulesPageBadge(
    component: RulesPageComponent,
    isSelected: Boolean,
) {

}