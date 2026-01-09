package dev.lounres.halfhat.client.ui.implementation.home

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.ui.components.home.HomePageComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.HomePageIcon
import dev.lounres.halfhat.client.ui.icons.HomePageSelectedIcon
import dev.lounres.halfhat.client.ui.utils.WorkInProgress
import dev.lounres.halfhat.client.ui.utils.commonIconModifier


@Composable
public fun HomePageIcon(
    isSelected: Boolean,
) {
    Icon(
        imageVector = if (isSelected) HalfHatIcon.HomePageSelectedIcon else HalfHatIcon.HomePageIcon,
        modifier = commonIconModifier,
        contentDescription = "Home page",
    )
}

@Composable
public fun HomePageBadge(
    component: HomePageComponent,
    isSelected: Boolean,
) {

}

@Composable
public fun HomePageUI(
    component: HomePageComponent,
) {
    WorkInProgress()
}