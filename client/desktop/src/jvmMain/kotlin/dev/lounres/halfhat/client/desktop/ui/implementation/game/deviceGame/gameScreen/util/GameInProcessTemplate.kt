package dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.util

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.desktop.resources.Res
import dev.lounres.halfhat.client.desktop.resources.deviceGameListenerIcon_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.deviceGameSpeakerIcon_dark_png_24dp
import org.jetbrains.compose.resources.painterResource


@Composable
fun GameInProcessTemplate(
    speaker: String,
    listener: String,
    timer: @Composable () -> Unit,
    word: String,
    onGuessed: () -> Unit,
    onNotGuessed: () -> Unit,
    onMistake: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
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
                        painter = painterResource(Res.drawable.deviceGameSpeakerIcon_dark_png_24dp),
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
                        painter = painterResource(Res.drawable.deviceGameListenerIcon_dark_png_24dp),
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
            // TODO: Rewrite this buggy shit
            //  1. Use logarithmic binary search instead of logarithmic descent.
            //  2. Make it also expand if needed, not only shrink
            //  3. Reset the `readyToDraw` to initial state when the sizes of the column are changed
            val currentTextStyle = LocalTextStyle.current.copy(fontSize = 1000.sp)
            var textStyle by remember { mutableStateOf(currentTextStyle) }
            var readyToDraw by remember { mutableStateOf(false) }
            Text(
                text = word,
                style = textStyle,
                softWrap = false,
                maxLines = 1,
                modifier = Modifier.drawWithContent {
                    if (readyToDraw) drawContent()
                },
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.hasVisualOverflow) {
                        textStyle = textStyle.copy(fontSize = textStyle.fontSize * 0.9)
                    } else {
                        readyToDraw = true
                    }
                }
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