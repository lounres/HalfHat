package dev.lounres.halfhat.client.ui.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.ui.icons.DeviceGameListenerIcon
import dev.lounres.halfhat.client.ui.icons.DeviceGameSpeakerIcon
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon


@Composable
public fun DeviceGameInProcessTemplate(
    speaker: String,
    listener: String,
    timer: @Composable () -> Unit,
    word: String,
    onGuessed: () -> Unit,
    onNotGuessed: () -> Unit,
    onMistake: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().widthIn(max = 480.dp).align(Alignment.Center).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = HalfHatIcon.DeviceGameSpeakerIcon,
                            modifier = Modifier.size(24.dp),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$speaker explains",
                            fontSize = 16.sp,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "$listener guesses",
                            fontSize = 16.sp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = HalfHatIcon.DeviceGameListenerIcon,
                            modifier = Modifier.size(24.dp),
                            contentDescription = null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    timer()
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = word,
                    autoSize = TextAutoSize.StepBased(),
                    softWrap = false,
                    maxLines = 1,
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        onClick = onNotGuessed,
                    ) {
                        Text(
                            text = "Not guessed",
                            fontSize = 16.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        onClick = onMistake,
                    ) {
                        Text(
                            text = "Mistake",
                            fontSize = 16.sp,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    onClick = onGuessed,
                ) {
                    Text(
                        text = "Guessed",
                        fontSize = 32.sp,
                    )
                }
            }
        }
    }
}