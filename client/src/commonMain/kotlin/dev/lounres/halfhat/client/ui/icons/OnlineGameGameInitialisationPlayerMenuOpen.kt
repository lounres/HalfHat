package dev.lounres.halfhat.client.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val HalfHatIcon.OnlineGameGameInitialisationPlayerMenuOpen: ImageVector by
        lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "OnlineGameGameInitialisationPlayerMenuOpen",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
    ).apply {
        path(fill = SolidColor(Color(0xFFE3E3E3))) {
            moveTo(480f, 616f)
            lineTo(240f, 376f)
            lineToRelative(56f, -56f)
            lineToRelative(184f, 184f)
            lineToRelative(184f, -184f)
            lineToRelative(56f, 56f)
            lineToRelative(-240f, 240f)
            close()
        }
    }.build()
}
