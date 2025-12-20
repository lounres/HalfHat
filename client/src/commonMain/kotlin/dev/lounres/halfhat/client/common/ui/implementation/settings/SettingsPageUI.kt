package dev.lounres.halfhat.client.common.ui.implementation.settings

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.settingsPage_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.settings.SettingsPageComponent
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
public fun SettingsPageIcon(
    isSelected: Boolean,
) {
    Icon(
        painter = painterResource(Res.drawable.settingsPage_dark_png_24dp),
        modifier = commonIconModifier,
        contentDescription = "Settings page",
    )
}

@Composable
public fun SettingsPageBadge(
    component: SettingsPageComponent,
    isSelected: Boolean,
) {

}