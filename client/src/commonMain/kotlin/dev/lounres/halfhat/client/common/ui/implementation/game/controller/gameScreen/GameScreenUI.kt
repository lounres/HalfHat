package dev.lounres.halfhat.client.common.ui.implementation.game.controller.gameScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.common.logic.components.game.timer.TimerState
import dev.lounres.halfhat.client.resources.Res
import dev.lounres.halfhat.client.resources.deviceGameListenerIcon_dark_png_24dp
import dev.lounres.halfhat.client.resources.deviceGameSpeakerIcon_dark_png_24dp
import dev.lounres.halfhat.client.resources.exitGameTimerButton_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.game.controller.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.common.ui.utils.AutoScalingText
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import dev.lounres.kone.hub.subscribeAsState
import org.jetbrains.compose.resources.painterResource
import kotlin.math.min


@Composable
public fun RowScope.GameScreenActionsUI(
    component: GameScreenComponent,
) {
    IconButton(
        onClick = component.onExitGameController
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(Res.drawable.exitGameTimerButton_dark_png_24dp),
            contentDescription = "Exit game controller"
        )
    }
}

@Composable
public fun GameScreenUI(
    component: GameScreenComponent,
) {
    when (val timerState = component.timerState.subscribeAsState().value) {
        TimerState.Finished ->
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().widthIn(max = 480.dp).align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.Start),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.deviceGameSpeakerIcon_dark_png_24dp),
                                modifier = Modifier.size(24.dp),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${component.speaker.subscribeAsState().value} explains",
                                fontSize = 24.sp,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Text(
                                text = "${component.listener.subscribeAsState().value} guesses",
                                fontSize = 24.sp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(Res.drawable.deviceGameListenerIcon_dark_png_24dp),
                                modifier = Modifier.size(24.dp),
                                contentDescription = null
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(0.4847561f),
                        shape = CircleShape,
                        onClick = component.onStartTimer,
                    ) {
                        Text(
                            text = "Start",
                            fontSize = 24.sp,
                        )
                    }
                }
            }
        is TimerState.Preparation ->
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().widthIn(max = 480.dp).align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
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
                                    text = "${component.speaker.subscribeAsState().value} explains",
                                    fontSize = 16.sp,
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${component.listener.subscribeAsState().value} guesses",
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
                    }
                    
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        val millisecondsLeft = timerState.millisecondsLeft
                        Text(
                            text = millisecondsLeft.let { if (it % 1000u != 0u) it / 1000u + 1u else it / 1000u }.toString(),
                            fontSize = 256.sp,
                            color = Color.hsv(
                                hue = min(millisecondsLeft, 3_000u).toInt() * 0.04f,
                                saturation = 1f,
                                value = 1f,
                            ),
                        )
                    }
                    
                    Button(
                        modifier = Modifier.fillMaxWidth(0.4847561f),
                        shape = CircleShape,
                        onClick = component.onFinishTimer,
                    ) {
                        Text(
                            text = "Finish",
                            fontSize = 24.sp,
                        )
                    }
                }
            }
        is TimerState.Explanation ->
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().widthIn(max = 480.dp).align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
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
                                text = "${component.speaker.subscribeAsState().value} explains",
                                fontSize = 16.sp,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${component.listener.subscribeAsState().value} guesses",
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
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val secondsLeft = timerState.millisecondsLeft.let { if (it % 1000u != 0u) it / 1000u + 1u else it / 1000u }
                        val secondsToShow = secondsLeft % 60u
                        val minutesToShow = secondsLeft / 60u
                        Text(
                            text = "${minutesToShow.toString().padStart(2, '0')}:${secondsToShow.toString().padStart(2, '0')}",
                            fontSize = 128.sp,
                        )
                    }
                    
                    Button(
                        modifier = Modifier.fillMaxWidth(0.4847561f),
                        shape = CircleShape,
                        onClick = component.onFinishTimer,
                    ) {
                        Text(
                            text = "Finish",
                            fontSize = 24.sp,
                        )
                    }
                }
            }
        is TimerState.LastGuess ->
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().widthIn(max = 480.dp).align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
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
                                text = "${component.speaker.subscribeAsState().value} explains",
                                fontSize = 16.sp,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${component.listener.subscribeAsState().value} guesses",
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
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val decisecondsLeft = timerState.millisecondsLeft.let { if (it % 100u != 0u) it / 100u + 1u else it / 100u }
                        val decisecondsToShow = decisecondsLeft % 10u
                        val secondsToShow = decisecondsLeft / 10u
                        Text(
                            text = "$secondsToShow.$decisecondsToShow",
                            fontSize = 128.sp,
                            color = Color.Red
                        )
                    }
                    
                    Button(
                        modifier = Modifier.fillMaxWidth(0.4847561f),
                        shape = CircleShape,
                        onClick = component.onFinishTimer,
                    ) {
                        Text(
                            text = "Finish",
                            fontSize = 24.sp,
                        )
                    }
                }
            }
    }
}