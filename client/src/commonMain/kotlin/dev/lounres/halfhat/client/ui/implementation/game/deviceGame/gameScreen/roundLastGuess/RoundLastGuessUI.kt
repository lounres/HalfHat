package dev.lounres.halfhat.client.ui.implementation.game.deviceGame.gameScreen.roundLastGuess

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.ui.components.game.deviceGame.gameScreen.roundLastGuess.RoundLastGuessComponent
import dev.lounres.halfhat.client.ui.icons.DeviceGameExitModeButton
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.utils.DeviceGameInProcessTemplate
import dev.lounres.halfhat.client.ui.utils.commonIconModifier


@Composable
public fun RowScope.RoundLastGuessActionsUI(
    component: RoundLastGuessComponent,
) {
    IconButton(
        onClick = component.onExitGame
    ) {
        Icon(
            modifier = commonIconModifier,
            imageVector = HalfHatIcon.DeviceGameExitModeButton,
            contentDescription = "Exit device game"
        )
    }
}

@Composable
public fun RoundLastGuessUI(
    component: RoundLastGuessComponent,
) {
    DeviceGameInProcessTemplate(
        speaker = component.speaker.collectAsState().value,
        listener = component.listener.collectAsState().value,
        timer = {
            val decisecondsLeft = component.millisecondsLeft.collectAsState().value.let { if (it % 100u != 0u) it / 100u + 1u else it / 100u }
            val decisecondsToShow = decisecondsLeft % 10u
            val secondsToShow = decisecondsLeft / 10u
            Text(
                text = "$secondsToShow.$decisecondsToShow",
                fontSize = 128.sp,
                color = Color.Red
            )
        },
        word = component.word.collectAsState().value,
        onGuessed = component.onGuessed,
        onNotGuessed = component.onNotGuessed,
        onMistake = component.onMistake,
    )
}