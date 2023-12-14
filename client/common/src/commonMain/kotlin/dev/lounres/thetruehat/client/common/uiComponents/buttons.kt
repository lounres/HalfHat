package dev.lounres.thetruehat.client.common.uiComponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
public fun CircleButton(modifier: Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    OutlinedIconButton(
        modifier = modifier
            .padding(10.dp)
            .size(50.dp),
        border = BorderStroke(1.dp, Color(160, 160, 160)),
        onClick = onClick,
        content = content
    )
}

@Composable
public fun CircleButtonWithIcon(modifier: Modifier = Modifier, icon: Painter, onClick: () -> Unit) {
    CircleButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(30.dp).background(color = Color.White.copy(alpha = 0f)),
            tint = Color(120, 120, 120)
        )
    }
}

@Composable
public fun CircleButtonWithIcon(modifier: Modifier = Modifier, icon: ImageVector, onClick: () -> Unit) {
    CircleButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            tint = Color(120, 120, 120)
        )
    }
}

@Composable
public fun CircleButtonWithImage(modifier: Modifier = Modifier, image: Painter, onClick: () -> Unit) {
    CircleButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Image(
            painter = image,
            contentDescription = null,
            modifier = Modifier.size(30.dp),
        )
    }
}

@Composable
public fun CircleButtonWithImage(modifier: Modifier = Modifier, image: ImageVector, onClick: () -> Unit) {
    CircleButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Image(
            imageVector = image,
            contentDescription = null,
            modifier = Modifier.size(30.dp),
        )
    }
}