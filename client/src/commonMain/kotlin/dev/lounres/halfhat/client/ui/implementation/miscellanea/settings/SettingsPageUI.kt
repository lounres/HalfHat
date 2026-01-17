package dev.lounres.halfhat.client.ui.implementation.miscellanea.settings

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.ui.components.miscellanea.settings.SettingsPageComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.SettingsPageIcon
import dev.lounres.halfhat.client.ui.icons.SettingsPageSelectedIcon
import dev.lounres.halfhat.client.ui.utils.commonIconModifier


@Composable
public fun SettingsPageIcon(
    isSelected: Boolean,
) {
    Icon(
        imageVector = if (isSelected) HalfHatIcon.SettingsPageSelectedIcon else HalfHatIcon.SettingsPageIcon,
        modifier = commonIconModifier,
        contentDescription = "Settings page",
    )
}

@Composable
public fun SettingsPageBadge(
    component: dev.lounres.halfhat.client.ui.components.miscellanea.settings.SettingsPageComponent,
    isSelected: Boolean,
) {

}