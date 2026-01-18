package dev.lounres.halfhat.client.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val HalfHatIcon.OnlineGamePlayersWordsAddWord: ImageVector by
        lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "OnlineGamePlayersWordsAddWord",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
    ).apply {
        path(fill = SolidColor(Color(0xFFE3E3E3))) {
            moveTo(440f, 840f)
            verticalLineToRelative(-320f)
            lineTo(120f, 520f)
            verticalLineToRelative(-80f)
            horizontalLineToRelative(320f)
            verticalLineToRelative(-320f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(320f)
            horizontalLineToRelative(320f)
            verticalLineToRelative(80f)
            lineTo(520f, 520f)
            verticalLineToRelative(320f)
            horizontalLineToRelative(-80f)
            close()
        }
    }.build()
}
