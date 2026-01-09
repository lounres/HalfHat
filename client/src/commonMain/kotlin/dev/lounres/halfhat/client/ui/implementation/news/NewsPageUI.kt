package dev.lounres.halfhat.client.ui.implementation.news

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.ui.components.news.NewsPageComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.NewsPageIcon
import dev.lounres.halfhat.client.ui.utils.commonIconModifier


@Composable
public fun NewsPageIcon(
    isSelected: Boolean,
) {
    Icon(
        imageVector = HalfHatIcon.NewsPageIcon,
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