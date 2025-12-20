package dev.lounres.halfhat.client.common.ui.implementation.game.deviceGame.gameScreen.roundExplanation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.exitDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundExplanation.RoundExplanationComponent
import dev.lounres.halfhat.client.common.ui.utils.GameInProcessTemplate
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
public fun RowScope.RoundExplanationActionsUI(
    component: RoundExplanationComponent,
) {
    IconButton(
        onClick = component.onExitGame
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(Res.drawable.exitDeviceGameButton_dark_png_24dp),
            contentDescription = "Exit device game"
        )
    }
}

@Composable
public fun RoundExplanationUI(
    component: RoundExplanationComponent,
) {
    GameInProcessTemplate(
        speaker = component.speaker.collectAsState().value,
        listener = component.listener.collectAsState().value,
        timer = {
            val secondsLeft = component.millisecondsLeft.collectAsState().value.let { if (it % 1000u != 0u) it / 1000u + 1u else it / 1000u }
            val secondsToShow = secondsLeft % 60u
            val minutesToShow = secondsLeft / 60u
            Text(
                text = "${minutesToShow.toString().padStart(2, '0')}:${secondsToShow.toString().padStart(2, '0')}",
                fontSize = 128.sp,
            )
        },
        word = component.word.collectAsState().value,
        onGuessed = component.onGuessed,
        onNotGuessed = component.onNotGuessed,
        onMistake = component.onMistake,
    )
}