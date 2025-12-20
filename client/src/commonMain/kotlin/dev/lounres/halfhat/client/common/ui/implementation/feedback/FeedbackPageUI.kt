package dev.lounres.halfhat.client.common.ui.implementation.feedback

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.feedbackPage_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.feedback.FeedbackPageComponent
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
public fun FeedbackPageIcon(
    isSelected: Boolean,
) {
    Icon(
        painter = painterResource(Res.drawable.feedbackPage_dark_png_24dp),
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