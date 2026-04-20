package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.RoundScreenComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameCopyKeyButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameCopyLinkButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameExitRoomButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameFinishGameButton
import dev.lounres.halfhat.client.ui.utils.commonIconModifier


fun RoundScreenToolbarContentUI(
    component: RoundScreenComponent,
): @Composable RowScope.() -> Unit = {
    IconButton(
        onClick = component.onExitOnlineGame
    ) {
        Icon(
            modifier = commonIconModifier,
            imageVector = HalfHatIcon.OnlineGameExitRoomButton,
            contentDescription = "Exit online game room"
        )
    }
    IconButton(
        onClick = component.onCopyOnlineGameKey
    ) {
        Icon(
            modifier = commonIconModifier,
            imageVector = HalfHatIcon.OnlineGameCopyKeyButton,
            contentDescription = "Copy online game room key"
        )
    }
    IconButton(
        onClick = component.onCopyOnlineGameLink
    ) {
        Icon(
            modifier = commonIconModifier,
            imageVector = HalfHatIcon.OnlineGameCopyLinkButton,
            contentDescription = "Copy online game room link"
        )
    }

    when (val role = component.gameState.collectAsState().value.selfRole) {
        is ServerApi.OnlineGame.SelfRole.Round.Waiting ->
            if (role.isGameFinishable)
                IconButton(
                    onClick = component.onFinishGame
                ) {
                    Icon(
                        modifier = commonIconModifier,
                        imageVector = HalfHatIcon.OnlineGameFinishGameButton,
                        contentDescription = "Finish online game"
                    )
                }
        is ServerApi.OnlineGame.SelfRole.Round.Preparation -> {}
        is ServerApi.OnlineGame.SelfRole.Round.Explanation -> {}
        is ServerApi.OnlineGame.SelfRole.Round.LastGuess -> {}
        is ServerApi.OnlineGame.SelfRole.Round.Editing -> {}
    }
}