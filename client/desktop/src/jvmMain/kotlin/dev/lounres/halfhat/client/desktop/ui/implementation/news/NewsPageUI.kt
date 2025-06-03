package dev.lounres.halfhat.client.desktop.ui.implementation.news

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import dev.lounres.halfhat.client.desktop.resources.newsPage_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.news.NewsPageComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
fun NewsPageIcon(
    isSelected: Boolean,
) {
    Icon(
        painter = painterResource(DesktopRes.drawable.newsPage_dark_png_24dp),
        modifier = commonIconModifier,
        contentDescription = "News page",
    )
}

@Composable
fun NewsPageBadge(
    component: NewsPageComponent,
    isSelected: Boolean,
) {

}