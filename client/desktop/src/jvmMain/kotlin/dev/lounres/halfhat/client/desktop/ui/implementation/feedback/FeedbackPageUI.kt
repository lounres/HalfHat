package dev.lounres.halfhat.client.desktop.ui.implementation.feedback

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import dev.lounres.halfhat.client.desktop.resources.feedbackPage_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.feedback.FeedbackPageComponent
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
fun FeedbackPageIcon(
    isSelected: Boolean,
) {
    Icon(
        painter = painterResource(DesktopRes.drawable.feedbackPage_dark_png_24dp),
        modifier = commonIconModifier,
        contentDescription = "Feedback page",
    )
}

@Composable
fun FeedbackPageBadge(
    component: FeedbackPageComponent,
    isSelected: Boolean,
) {

}