package dev.lounres.halfhat.client.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val HalfHatIcon.OnlineGameSpeakerToListenerDownArrow: ImageVector by
        lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "OnlineGameSpeakerToListenerDownArrow",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
    ).apply {
        path(fill = SolidColor(Color(0xFFE3E3E3))) {
            moveTo(480f, 720f)
            lineTo(240f, 480f)
            lineToRelative(56f, -56f)
            lineToRelative(144f, 144f)
            verticalLineToRelative(-368f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(368f)
            lineToRelative(144f, -144f)
            lineToRelative(56f, 56f)
            lineToRelative(-240f, 240f)
            close()
        }
    }.build()
}
