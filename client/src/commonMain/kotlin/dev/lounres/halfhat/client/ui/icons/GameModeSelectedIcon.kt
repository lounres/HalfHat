package dev.lounres.halfhat.client.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val HalfHatIcon.GameModeSelectedIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "GameModeSelectedIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
    ).apply {
        path(fill = SolidColor(Color(0xFFE3E3E3))) {
            moveTo(182f, 760f)
            quadToRelative(-51f, 0f, -79f, -35.5f)
            reflectiveQuadTo(82f, 638f)
            lineToRelative(42f, -300f)
            quadToRelative(9f, -60f, 53.5f, -99f)
            reflectiveQuadTo(282f, 200f)
            horizontalLineToRelative(396f)
            quadToRelative(60f, 0f, 104.5f, 39f)
            reflectiveQuadToRelative(53.5f, 99f)
            lineToRelative(42f, 300f)
            quadToRelative(7f, 51f, -21f, 86.5f)
            reflectiveQuadTo(778f, 760f)
            quadToRelative(-21f, 0f, -39f, -7.5f)
            reflectiveQuadTo(706f, 730f)
            lineToRelative(-90f, -90f)
            lineTo(344f, 640f)
            lineToRelative(-90f, 90f)
            quadToRelative(-15f, 15f, -33f, 22.5f)
            reflectiveQuadToRelative(-39f, 7.5f)
            close()
            moveTo(680f, 520f)
            quadToRelative(17f, 0f, 28.5f, -11.5f)
            reflectiveQuadTo(720f, 480f)
            quadToRelative(0f, -17f, -11.5f, -28.5f)
            reflectiveQuadTo(680f, 440f)
            quadToRelative(-17f, 0f, -28.5f, 11.5f)
            reflectiveQuadTo(640f, 480f)
            quadToRelative(0f, 17f, 11.5f, 28.5f)
            reflectiveQuadTo(680f, 520f)
            close()
            moveTo(600f, 400f)
            quadToRelative(17f, 0f, 28.5f, -11.5f)
            reflectiveQuadTo(640f, 360f)
            quadToRelative(0f, -17f, -11.5f, -28.5f)
            reflectiveQuadTo(600f, 320f)
            quadToRelative(-17f, 0f, -28.5f, 11.5f)
            reflectiveQuadTo(560f, 360f)
            quadToRelative(0f, 17f, 11.5f, 28.5f)
            reflectiveQuadTo(600f, 400f)
            close()
            moveTo(310f, 450f)
            verticalLineToRelative(40f)
            quadToRelative(0f, 13f, 8.5f, 21.5f)
            reflectiveQuadTo(340f, 520f)
            quadToRelative(13f, 0f, 21.5f, -8.5f)
            reflectiveQuadTo(370f, 490f)
            verticalLineToRelative(-40f)
            horizontalLineToRelative(40f)
            quadToRelative(13f, 0f, 21.5f, -8.5f)
            reflectiveQuadTo(440f, 420f)
            quadToRelative(0f, -13f, -8.5f, -21.5f)
            reflectiveQuadTo(410f, 390f)
            horizontalLineToRelative(-40f)
            verticalLineToRelative(-40f)
            quadToRelative(0f, -13f, -8.5f, -21.5f)
            reflectiveQuadTo(340f, 320f)
            quadToRelative(-13f, 0f, -21.5f, 8.5f)
            reflectiveQuadTo(310f, 350f)
            verticalLineToRelative(40f)
            horizontalLineToRelative(-40f)
            quadToRelative(-13f, 0f, -21.5f, 8.5f)
            reflectiveQuadTo(240f, 420f)
            quadToRelative(0f, 13f, 8.5f, 21.5f)
            reflectiveQuadTo(270f, 450f)
            horizontalLineToRelative(40f)
            close()
        }
    }.build()
}
