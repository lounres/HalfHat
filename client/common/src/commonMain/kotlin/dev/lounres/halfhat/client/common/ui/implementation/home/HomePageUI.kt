package dev.lounres.halfhat.client.common.ui.implementation.home

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.homePage_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.home.HomePageComponent
import dev.lounres.halfhat.client.common.ui.utils.WorkInProgress
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
public fun HomePageIcon(
    isSelected: Boolean,
) {
    Icon(
        painter = painterResource(Res.drawable.homePage_dark_png_24dp),
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