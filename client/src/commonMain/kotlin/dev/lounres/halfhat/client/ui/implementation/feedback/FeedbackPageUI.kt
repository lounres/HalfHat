package dev.lounres.halfhat.client.ui.implementation.feedback

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.ui.components.feedback.FeedbackPageComponent
import dev.lounres.halfhat.client.ui.icons.FaqPageIcon
import dev.lounres.halfhat.client.ui.icons.FaqPageSelectedIcon
import dev.lounres.halfhat.client.ui.icons.FeedbackPageIcon
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.utils.commonIconModifier


@Composable
public fun FeedbackPageIcon(
    isSelected: Boolean,
) {
    Icon(
        imageVector = if (isSelected) HalfHatIcon.FaqPageSelectedIcon else HalfHatIcon.FeedbackPageIcon,
        modifier = commonIconModifier,
        contentDescription = "Feedback page",
    )
}

@Composable
public fun FeedbackPageBadge(
    component: FeedbackPageComponent,
    isSelected: Boolean,
) {

}