package dev.lounres.halfhat.client.desktop.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.desktop.resources.workInProgressIcon_dark_png_24dp
import org.jetbrains.compose.resources.painterResource
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes


@Composable
fun WorkInProgress(modifier: Modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(DesktopRes.drawable.workInProgressIcon_dark_png_24dp),
            modifier = Modifier,
            contentDescription = null
        )
        Text(
            text = "Work In Progress",
            fontSize = 32.sp,
        )
        Text(
            text = "Sorry, this part is not yet implemented :(",
            fontSize = 16.sp,
        )
    }
}