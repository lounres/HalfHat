package dev.lounres.halfhat.client.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val HalfHatIcon.FeedbackPageSelectedIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "FeedbackPageSelectedIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
    ).apply {
        path(fill = SolidColor(Color(0xFFE3E3E3))) {
            moveToRelative(240f, 720f)
            lineToRelative(-92f, 92f)
            quadToRelative(-19f, 19f, -43.5f, 8.5f)
            reflectiveQuadTo(80f, 783f)
            verticalLineToRelative(-623f)
            quadToRelative(0f, -33f, 23.5f, -56.5f)
            reflectiveQuadTo(160f, 80f)
            horizontalLineToRelative(640f)
            quadToRelative(33f, 0f, 56.5f, 23.5f)
            reflectiveQuadTo(880f, 160f)
            verticalLineToRelative(480f)
            quadToRelative(0f, 33f, -23.5f, 56.5f)
            reflectiveQuadTo(800f, 720f)
            lineTo(240f, 720f)
            close()
            moveTo(480f, 600f)
            quadToRelative(17f, 0f, 28.5f, -11.5f)
            reflectiveQuadTo(520f, 560f)
            quadToRelative(0f, -17f, -11.5f, -28.5f)
            reflectiveQuadTo(480f, 520f)
            quadToRelative(-17f, 0f, -28.5f, 11.5f)
            reflectiveQuadTo(440f, 560f)
            quadToRelative(0f, 17f, 11.5f, 28.5f)
            reflectiveQuadTo(480f, 600f)
            close()
            moveTo(480f, 440f)
            quadToRelative(17f, 0f, 28.5f, -11.5f)
            reflectiveQuadTo(520f, 400f)
            verticalLineToRelative(-160f)
            quadToRelative(0f, -17f, -11.5f, -28.5f)
            reflectiveQuadTo(480f, 200f)
            quadToRelative(-17f, 0f, -28.5f, 11.5f)
            reflectiveQuadTo(440f, 240f)
            verticalLineToRelative(160f)
            quadToRelative(0f, 17f, 11.5f, 28.5f)
            reflectiveQuadTo(480f, 440f)
            close()
        }
    }.build()
}
