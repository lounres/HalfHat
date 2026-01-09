package dev.lounres.halfhat.client.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.WorkInProgressIcon


@Composable
public fun WorkInProgress(modifier: Modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = HalfHatIcon.WorkInProgressIcon,
            modifier = Modifier.size(120.dp),
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