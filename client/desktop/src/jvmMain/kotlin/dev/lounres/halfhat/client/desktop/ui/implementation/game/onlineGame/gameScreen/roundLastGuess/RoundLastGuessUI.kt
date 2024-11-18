package dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roundLastGuess

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import dev.lounres.halfhat.client.desktop.resources.exitDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.onlineGameKey_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.onlineGameLink_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.commonIconModifier
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.util.GameInProcessTemplate
import org.jetbrains.compose.resources.painterResource


@Composable
fun RowScope.RoundLastGuessActionsUI(
    component: RoundLastGuessComponent,
) {
    IconButton(
        onClick = component.onCopyOnlineGameKey
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.onlineGameKey_dark_png_24dp),
            contentDescription = "Copy online game key"
        )
    }
    IconButton(
        onClick = component.onCopyOnlineGameLink
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.onlineGameLink_dark_png_24dp),
            contentDescription = "Copy online game link"
        )
    }
    IconButton(
        onClick = component.onExitOnlineGame
    ) {
        Icon(
            modifier = commonIconModifier,
            painter = painterResource(DesktopRes.drawable.exitDeviceGameButton_dark_png_24dp), // TODO: Copy the icons
            contentDescription = "Exit online game"
        )
    }
}

@Composable
fun RoundLastGuessUI(
    component: RoundLastGuessComponent,
) {
    val gameState by component.gameState.collectAsState()
    GameInProcessTemplate(
        speaker = gameState.playersList[gameState.speakerIndex].name,
        listener = gameState.playersList[gameState.listenerIndex].name,
        timer = {
            Text(
                text = (gameState.millisecondsLeft / 100u + 1u).let { "${it / 10u}.${it % 10u}" },
                fontSize = 64.sp,
                color = Color.Red
            )
        },
        word = TODO(),
        onGuessed = component.onGuessed,
        onNotGuessed = component.onNotGuessed,
        onMistake = component.onMistake,
    )
}