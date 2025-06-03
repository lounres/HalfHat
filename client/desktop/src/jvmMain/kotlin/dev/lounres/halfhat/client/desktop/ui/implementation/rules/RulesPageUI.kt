package dev.lounres.halfhat.client.desktop.ui.implementation.rules

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import dev.lounres.halfhat.client.desktop.resources.rulesPage_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.rules.RulesPageComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
fun RulesPageIcon(
    isSelected: Boolean,
) {
    Icon(
        painter = painterResource(DesktopRes.drawable.rulesPage_dark_png_24dp),
        modifier = commonIconModifier,
        contentDescription = "Rules page",
    )
}

@Composable
fun RulesPageBadge(
    component: RulesPageComponent,
    isSelected: Boolean,
) {

}