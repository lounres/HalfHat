package dev.lounres.thetruehat.client.common.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp


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

@Composable
public fun Table(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    verticalLazyListState: LazyListState = rememberLazyListState(),
    horizontalScrollState: ScrollState = rememberScrollState(),
    columnCount: Int,
    rowCount: Int,
    beforeRow: (@Composable (rowIndex: Int) -> Unit)? = null,
    afterRow: (@Composable (rowIndex: Int) -> Unit)? = null,
    cellContent: @Composable (columnIndex: Int, rowIndex: Int) -> Unit
) {
    val columnWidths = remember { mutableStateMapOf<Int, Int>() }

    Box(modifier = modifier.then(Modifier.horizontalScroll(horizontalScrollState))) {
        LazyColumn(state = verticalLazyListState) {
            items(rowCount) { rowIndex ->
                Column {
                    beforeRow?.invoke(rowIndex)

                    Row(modifier = rowModifier) {
                        (0 until columnCount).forEach { columnIndex ->
                            Box(modifier = Modifier.layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)

                                val existingWidth = columnWidths[columnIndex] ?: 0
                                val maxWidth = maxOf(existingWidth, placeable.width)

                                if (maxWidth > existingWidth) {
                                    columnWidths[columnIndex] = maxWidth
                                }

                                layout(width = maxWidth, height = placeable.height) {
                                    placeable.placeRelative(0, 0)
                                }
                            }) {
                                cellContent(columnIndex, rowIndex)
                            }
                        }
                    }

                    afterRow?.invoke(rowIndex)
                }
            }
        }
    }
}