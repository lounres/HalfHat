package dev.lounres.halfhat.client.ui.implementation.news

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.resources.Res
import dev.lounres.halfhat.client.resources.newsPage_dark_png_24dp
import dev.lounres.halfhat.client.ui.components.news.NewsPageComponent
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
public fun NewsPageIcon(
    isSelected: Boolean,
) {
    Icon(
        painter = painterResource(Res.drawable.newsPage_dark_png_24dp),
        modifier = commonIconModifier,
        contentDescription = "News page",
    )
}

@Composable
public fun NewsPageBadge(
    component: NewsPageComponent,
    isSelected: Boolean,
) {

}