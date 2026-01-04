package dev.lounres.halfhat.client.ui.theming

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import dev.lounres.halfhat.client.resources.Res
import dev.lounres.halfhat.client.resources.RobotoFlex
import org.jetbrains.compose.resources.Font


actual val bodyFont: Font @Composable get() = Font(Res.font.RobotoFlex)

actual val displayFont: Font @Composable get() = Font(Res.font.RobotoFlex)