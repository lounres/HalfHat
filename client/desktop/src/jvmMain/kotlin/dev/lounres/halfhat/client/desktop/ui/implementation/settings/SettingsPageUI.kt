package dev.lounres.halfhat.client.desktop.ui.implementation.settings

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import dev.lounres.halfhat.client.desktop.resources.settingsPage_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.settings.SettingsPageComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
fun SettingsPageIcon(
    isSelected: Boolean,
) {
    Icon(
        painter = painterResource(DesktopRes.drawable.settingsPage_dark_png_24dp),
        modifier = commonIconModifier,
        contentDescription = "Settings page",
    )
}

@Composable
fun SettingsPageBadge(
    component: SettingsPageComponent,
    isSelected: Boolean,
) {

}