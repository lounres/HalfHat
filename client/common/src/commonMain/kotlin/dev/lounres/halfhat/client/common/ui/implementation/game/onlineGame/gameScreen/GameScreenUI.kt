package dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.exitDeviceGameButton_dark_png_24dp
import dev.lounres.halfhat.client.common.resources.onlineGameKey_dark_png_24dp
import dev.lounres.halfhat.client.common.resources.onlineGameLink_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.gameResults.GameResultsActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.gameResults.GameResultsButtonsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.gameResults.GameResultsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.loading.LoadingActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.loading.LoadingButtonsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.loading.LoadingUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roomScreen.RoomScreenActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roomScreen.RoomScreenButtonsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roomScreen.RoomScreenUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roomSettings.RoomSettingsActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roomSettings.RoomSettingsButtonsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roomSettings.RoomSettingsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundEditing.RoundEditingActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundEditing.RoundEditingButtonsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundEditing.RoundEditingUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundExplanation.RoundExplanationActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundExplanation.RoundExplanationButtonsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundExplanation.RoundExplanationUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessButtonsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundPreparation.RoundPreparationActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundPreparation.RoundPreparationButtonsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundPreparation.RoundPreparationUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundWaiting.RoundWaitingActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundWaiting.RoundWaitingButtonsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.onlineGame.gameScreen.roundWaiting.RoundWaitingUI
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import dev.lounres.kone.hub.subscribeAsState
import org.jetbrains.compose.resources.painterResource


@Composable
public fun RowScope.GameScreenActionsUI(
    component: GameScreenComponent
) {
    when (val child = component.childSlot.subscribeAsState().value.component) {
        is GameScreenComponent.Child.Loading -> LoadingActionsUI(child.component)
        is GameScreenComponent.Child.RoomScreen -> RoomScreenActionsUI(child.component)
        is GameScreenComponent.Child.RoomSettings -> RoomSettingsActionsUI(child.component)
        is GameScreenComponent.Child.RoundWaiting -> RoundWaitingActionsUI(child.component)
        is GameScreenComponent.Child.RoundPreparation -> RoundPreparationActionsUI(child.component)
        is GameScreenComponent.Child.RoundExplanation -> RoundExplanationActionsUI(child.component)
        is GameScreenComponent.Child.RoundLastGuess -> RoundLastGuessActionsUI(child.component)
        is GameScreenComponent.Child.RoundEditing -> RoundEditingActionsUI(child.component)
        is GameScreenComponent.Child.GameResults -> GameResultsActionsUI(child.component)
    }
}

@Composable
public fun ColumnScope.GameScreenUI(
    component: GameScreenComponent
) {
    val child = component.childSlot.subscribeAsState().value.component
    Column(
        modifier = Modifier.fillMaxWidth().weight(1f),
    ) {
        when (child) {
            is GameScreenComponent.Child.Loading -> LoadingUI(child.component)
            is GameScreenComponent.Child.RoomScreen -> RoomScreenUI(child.component)
            is GameScreenComponent.Child.RoomSettings -> RoomSettingsUI(child.component)
            is GameScreenComponent.Child.RoundWaiting -> RoundWaitingUI(child.component)
            is GameScreenComponent.Child.RoundPreparation -> RoundPreparationUI(child.component)
            is GameScreenComponent.Child.RoundExplanation -> RoundExplanationUI(child.component)
            is GameScreenComponent.Child.RoundLastGuess -> RoundLastGuessUI(child.component)
            is GameScreenComponent.Child.RoundEditing -> RoundEditingUI(child.component)
            is GameScreenComponent.Child.GameResults -> GameResultsUI(child.component)
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = CircleShape,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = component.onExitOnlineGame
            ) {
                Icon(
                    modifier = commonIconModifier,
                    painter = painterResource(Res.drawable.exitDeviceGameButton_dark_png_24dp), // TODO: Copy the icons
                    contentDescription = "Exit online game"
                )
            }
            IconButton(
                onClick = component.onCopyOnlineGameKey
            ) {
                Icon(
                    modifier = commonIconModifier,
                    painter = painterResource(Res.drawable.onlineGameKey_dark_png_24dp),
                    contentDescription = "Copy online game key"
                )
            }
            IconButton(
                onClick = component.onCopyOnlineGameLink
            ) {
                Icon(
                    modifier = commonIconModifier,
                    painter = painterResource(Res.drawable.onlineGameLink_dark_png_24dp),
                    contentDescription = "Copy online game link"
                )
            }
            when (child) {
                is GameScreenComponent.Child.Loading -> LoadingButtonsUI(child.component)
                is GameScreenComponent.Child.RoomScreen -> RoomScreenButtonsUI(child.component)
                is GameScreenComponent.Child.RoomSettings -> RoomSettingsButtonsUI(child.component)
                is GameScreenComponent.Child.RoundWaiting -> RoundWaitingButtonsUI(child.component)
                is GameScreenComponent.Child.RoundPreparation -> RoundPreparationButtonsUI(child.component)
                is GameScreenComponent.Child.RoundExplanation -> RoundExplanationButtonsUI(child.component)
                is GameScreenComponent.Child.RoundLastGuess -> RoundLastGuessButtonsUI(child.component)
                is GameScreenComponent.Child.RoundEditing -> RoundEditingButtonsUI(child.component)
                is GameScreenComponent.Child.GameResults -> GameResultsButtonsUI(child.component)
            }
        }
    }
}