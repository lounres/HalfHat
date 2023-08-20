package dev.lounres.thetruehat.client.common.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


public fun Modifier(builder: Modifier.() -> Unit): Modifier = Modifier.apply(builder)

@Composable
public fun CircleButton(modifier: Modifier, onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    OutlinedButton(
        modifier = modifier
            .padding(10.dp)
            .size(50.dp)
            .shadow(5.dp, shape = CircleShape),
        shape = CircleShape,
        border = BorderStroke(1.dp, Color(160, 160, 160)),
        contentPadding = PaddingValues(0.dp),
        onClick = onClick,
        content = content,
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
            modifier = Modifier.size(30.dp),
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