package dev.lounres.halfhat.client.desktop.ui.implementation.home

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import dev.lounres.halfhat.client.desktop.resources.homePage_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.home.HomePageComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import dev.lounres.halfhat.client.desktop.ui.utils.WorkInProgress
import org.jetbrains.compose.resources.painterResource


@Composable
fun HomePageIcon(
    isSelected: Boolean,
) {
    Icon(
        painter = painterResource(DesktopRes.drawable.homePage_dark_png_24dp),
        modifier = commonIconModifier,
        contentDescription = "Home page",
    )
}

@Composable
fun HomePageBadge(
    component: HomePageComponent,
    isSelected: Boolean,
) {

}

@Composable
fun HomePageUI(
    component: HomePageComponent,
) {
    WorkInProgress()
}