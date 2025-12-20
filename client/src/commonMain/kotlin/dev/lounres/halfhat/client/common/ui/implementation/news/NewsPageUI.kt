package dev.lounres.halfhat.client.common.ui.implementation.news

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.newsPage_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.news.NewsPageComponent
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
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