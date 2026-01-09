package dev.lounres.halfhat.client.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val HalfHatIcon.RulesPageIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "RulesPageIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
    ).apply {
        path(fill = SolidColor(Color(0xFFE3E3E3))) {
            moveTo(200f, 760f)
            horizontalLineToRelative(400f)
            quadToRelative(17f, 0f, 28.5f, 11.5f)
            reflectiveQuadTo(640f, 800f)
            quadToRelative(0f, 17f, -11.5f, 28.5f)
            reflectiveQuadTo(600f, 840f)
            lineTo(200f, 840f)
            quadToRelative(-17f, 0f, -28.5f, -11.5f)
            reflectiveQuadTo(160f, 800f)
            quadToRelative(0f, -17f, 11.5f, -28.5f)
            reflectiveQuadTo(200f, 760f)
            close()
            moveTo(329f, 589f)
            lineTo(216f, 476f)
            quadToRelative(-23f, -23f, -23.5f, -56.5f)
            reflectiveQuadTo(215f, 363f)
            lineToRelative(29f, -29f)
            lineToRelative(228f, 226f)
            lineToRelative(-29f, 29f)
            quadToRelative(-23f, 23f, -57f, 23f)
            reflectiveQuadToRelative(-57f, -23f)
            close()
            moveTo(640f, 392f)
            lineTo(414f, 164f)
            lineToRelative(29f, -29f)
            quadToRelative(23f, -23f, 56.5f, -22.5f)
            reflectiveQuadTo(556f, 136f)
            lineToRelative(113f, 113f)
            quadToRelative(23f, 23f, 23f, 57f)
            reflectiveQuadToRelative(-23f, 57f)
            lineToRelative(-29f, 29f)
            close()
            moveTo(796f, 772f)
            lineTo(302f, 278f)
            lineToRelative(56f, -56f)
            lineToRelative(494f, 494f)
            quadToRelative(11f, 11f, 11f, 28f)
            reflectiveQuadToRelative(-11f, 28f)
            quadToRelative(-11f, 11f, -28f, 11f)
            reflectiveQuadToRelative(-28f, -11f)
            close()
        }
    }.build()
}
