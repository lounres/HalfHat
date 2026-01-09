package dev.lounres.halfhat.client.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val HalfHatIcon.GameModeDeviceGameIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "GameModeDeviceGameIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
    ).apply {
        path(fill = SolidColor(Color(0xFFE3E3E3))) {
            moveTo(480f, 420f)
            close()
            moveTo(440f, 800f)
            lineTo(120f, 800f)
            quadToRelative(-17f, 0f, -28.5f, -11.5f)
            reflectiveQuadTo(80f, 760f)
            quadToRelative(0f, -17f, 11.5f, -28.5f)
            reflectiveQuadTo(120f, 720f)
            horizontalLineToRelative(320f)
            quadToRelative(17f, 0f, 28.5f, 11.5f)
            reflectiveQuadTo(480f, 760f)
            quadToRelative(0f, 17f, -11.5f, 28.5f)
            reflectiveQuadTo(440f, 800f)
            close()
            moveTo(200f, 680f)
            quadToRelative(-33f, 0f, -56.5f, -23.5f)
            reflectiveQuadTo(120f, 600f)
            verticalLineToRelative(-360f)
            quadToRelative(0f, -33f, 23.5f, -56.5f)
            reflectiveQuadTo(200f, 160f)
            horizontalLineToRelative(600f)
            quadToRelative(17f, 0f, 28.5f, 11.5f)
            reflectiveQuadTo(840f, 200f)
            quadToRelative(0f, 17f, -11.5f, 28.5f)
            reflectiveQuadTo(800f, 240f)
            lineTo(200f, 240f)
            verticalLineToRelative(360f)
            horizontalLineToRelative(240f)
            quadToRelative(17f, 0f, 28.5f, 11.5f)
            reflectiveQuadTo(480f, 640f)
            quadToRelative(0f, 17f, -11.5f, 28.5f)
            reflectiveQuadTo(440f, 680f)
            lineTo(200f, 680f)
            close()
            moveTo(800f, 720f)
            verticalLineToRelative(-320f)
            lineTo(640f, 400f)
            verticalLineToRelative(320f)
            horizontalLineToRelative(160f)
            close()
            moveTo(620f, 800f)
            quadToRelative(-25f, 0f, -42.5f, -17.5f)
            reflectiveQuadTo(560f, 740f)
            verticalLineToRelative(-360f)
            quadToRelative(0f, -25f, 17.5f, -42.5f)
            reflectiveQuadTo(620f, 320f)
            horizontalLineToRelative(200f)
            quadToRelative(25f, 0f, 42.5f, 17.5f)
            reflectiveQuadTo(880f, 380f)
            verticalLineToRelative(360f)
            quadToRelative(0f, 25f, -17.5f, 42.5f)
            reflectiveQuadTo(820f, 800f)
            lineTo(620f, 800f)
            close()
            moveTo(720f, 500f)
            quadToRelative(13f, 0f, 21.5f, -9f)
            reflectiveQuadToRelative(8.5f, -21f)
            quadToRelative(0f, -13f, -8.5f, -21.5f)
            reflectiveQuadTo(720f, 440f)
            quadToRelative(-12f, 0f, -21f, 8.5f)
            reflectiveQuadToRelative(-9f, 21.5f)
            quadToRelative(0f, 12f, 9f, 21f)
            reflectiveQuadToRelative(21f, 9f)
            close()
            moveTo(720f, 560f)
            close()
        }
    }.build()
}
