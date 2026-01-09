package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameCopyKeyButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameCopyLinkButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameExitRoomButton
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.gameResults.GameResultsActionsUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.gameResults.GameResultsFloatingActionButtonUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.gameResults.GameResultsToolbarUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.gameResults.GameResultsUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.loading.LoadingActionsUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.loading.LoadingFloatingActionButtonUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.loading.LoadingToolbarUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.loading.LoadingUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roomScreen.RoomScreenActionsUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roomScreen.RoomScreenToolbarUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roomScreen.RoomScreenFloatingActionButtonUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roomScreen.RoomScreenUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roomSettings.RoomSettingsActionsUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roomSettings.RoomSettingsFloatingActionButtonUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roomSettings.RoomSettingsToolbarUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roomSettings.RoomSettingsUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundEditing.RoundEditingActionsUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundEditing.RoundEditingFloatingActionButtonUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundEditing.RoundEditingToolbarUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundEditing.RoundEditingUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundExplanation.RoundExplanationActionsUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundExplanation.RoundExplanationFloatingActionButtonUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundExplanation.RoundExplanationToolbarUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundExplanation.RoundExplanationUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessActionsUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessFloatingActionButtonUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessToolbarUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundPreparation.RoundPreparationActionsUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundPreparation.RoundPreparationFloatingActionButtonUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundPreparation.RoundPreparationToolbarUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundPreparation.RoundPreparationUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundWaiting.RoundWaitingActionsUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundWaiting.RoundWaitingFloatingActionButtonUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundWaiting.RoundWaitingToolbarUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundWaiting.RoundWaitingUI
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.kone.hub.subscribeAsState


@Composable
public fun RowScope.GameScreenActionsUI(
    component: GameScreenComponent
) {
    when (val child = component.childSlot.subscribeAsState().value.component) {
        is GameScreenComponent.Child.Loading -> LoadingActionsUI(child.component)
        is GameScreenComponent.Child.RoomScreen -> RoomScreenActionsUI(child.component)
        is GameScreenComponent.Child.RoomSettings -> RoomSettingsActionsUI(child.component)
        is GameScreenComponent.Child.PlayersWordsCollection -> TODO()
        is GameScreenComponent.Child.RoundWaiting -> RoundWaitingActionsUI(child.component)
        is GameScreenComponent.Child.RoundPreparation -> RoundPreparationActionsUI(child.component)
        is GameScreenComponent.Child.RoundExplanation -> RoundExplanationActionsUI(child.component)
        is GameScreenComponent.Child.RoundLastGuess -> RoundLastGuessActionsUI(child.component)
        is GameScreenComponent.Child.RoundEditing -> RoundEditingActionsUI(child.component)
        is GameScreenComponent.Child.GameResults -> GameResultsActionsUI(child.component)
    }
}

@Composable
public fun ColumnScope.GameScreenContentUI(
    component: GameScreenComponent
) {
    Column(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val child = component.childSlot.subscribeAsState().value.component
        when (child) {
            is GameScreenComponent.Child.Loading -> LoadingUI(child.component)
            is GameScreenComponent.Child.RoomScreen -> RoomScreenUI(child.component)
            is GameScreenComponent.Child.RoomSettings -> RoomSettingsUI(child.component)
            is GameScreenComponent.Child.PlayersWordsCollection -> TODO()
            is GameScreenComponent.Child.RoundWaiting -> RoundWaitingUI(child.component)
            is GameScreenComponent.Child.RoundPreparation -> RoundPreparationUI(child.component)
            is GameScreenComponent.Child.RoundExplanation -> RoundExplanationUI(child.component)
            is GameScreenComponent.Child.RoundLastGuess -> RoundLastGuessUI(child.component)
            is GameScreenComponent.Child.RoundEditing -> RoundEditingUI(child.component)
            is GameScreenComponent.Child.GameResults -> GameResultsUI(child.component)
        }
    }
}

public fun GameScreenToolbarUI(
    component: GameScreenComponent
): @Composable (RowScope.() -> Unit) = {
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
    when (val child = component.childSlot.subscribeAsState().value.component) {
        is GameScreenComponent.Child.Loading -> LoadingToolbarUI(child.component)
        is GameScreenComponent.Child.RoomScreen -> RoomScreenToolbarUI(child.component)
        is GameScreenComponent.Child.RoomSettings -> RoomSettingsToolbarUI(child.component)
        is GameScreenComponent.Child.PlayersWordsCollection -> TODO()
        is GameScreenComponent.Child.RoundWaiting -> RoundWaitingToolbarUI(child.component)
        is GameScreenComponent.Child.RoundPreparation -> RoundPreparationToolbarUI(child.component)
        is GameScreenComponent.Child.RoundExplanation -> RoundExplanationToolbarUI(child.component)
        is GameScreenComponent.Child.RoundLastGuess -> RoundLastGuessToolbarUI(child.component)
        is GameScreenComponent.Child.RoundEditing -> RoundEditingToolbarUI(child.component)
        is GameScreenComponent.Child.GameResults -> GameResultsToolbarUI(child.component)
    }
}

@Composable
public fun GameScreenToolbarFloatingActionButtonUI(
    component: GameScreenComponent
): @Composable (() -> Unit)? = when (val child = component.childSlot.subscribeAsState().value.component) {
    is GameScreenComponent.Child.Loading -> LoadingFloatingActionButtonUI(child.component)
    is GameScreenComponent.Child.RoomScreen -> RoomScreenFloatingActionButtonUI(child.component)
    is GameScreenComponent.Child.RoomSettings -> RoomSettingsFloatingActionButtonUI(child.component)
    is GameScreenComponent.Child.PlayersWordsCollection -> TODO()
    is GameScreenComponent.Child.RoundWaiting -> RoundWaitingFloatingActionButtonUI(child.component)
    is GameScreenComponent.Child.RoundPreparation -> RoundPreparationFloatingActionButtonUI(child.component)
    is GameScreenComponent.Child.RoundExplanation -> RoundExplanationFloatingActionButtonUI(child.component)
    is GameScreenComponent.Child.RoundLastGuess -> RoundLastGuessFloatingActionButtonUI(child.component)
    is GameScreenComponent.Child.RoundEditing -> RoundEditingFloatingActionButtonUI(child.component)
    is GameScreenComponent.Child.GameResults -> GameResultsFloatingActionButtonUI(child.component)
}

@Composable
public fun ColumnScope.GameScreenUI(
    component: GameScreenComponent
) {
    GameScreenContentUI(component)
    val toolbarModifier = Modifier
    val toolbarColors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()
    val toolbarExpanded = true
    val toolbarFloatingActionButton = GameScreenToolbarFloatingActionButtonUI(component)
    val toolbarContent = GameScreenToolbarUI(component)
    if (toolbarFloatingActionButton != null)
        HorizontalFloatingToolbar(
            modifier = toolbarModifier,
            colors = toolbarColors,
            expanded = toolbarExpanded,
            floatingActionButton = toolbarFloatingActionButton,
            content = toolbarContent,
        )
    else
        HorizontalFloatingToolbar(
            modifier = toolbarModifier.padding(vertical = 8.dp),
            colors = toolbarColors,
            expanded = toolbarExpanded,
            content = toolbarContent,
        )
    Spacer(modifier = Modifier.height(16.dp))
}