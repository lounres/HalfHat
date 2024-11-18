package dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.gameResults.GameResultsActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.gameResults.GameResultsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.loading.LoadingActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.loading.LoadingUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roomScreen.RoomScreenActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roomScreen.RoomScreenUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roomSettings.RoomSettingsActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roomSettings.RoomSettingsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roundEditing.RoundEditingActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roundEditing.RoundEditingUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roundExplanation.RoundExplanationActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roundExplanation.RoundExplanationUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roundPreparation.RoundPreparationActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roundPreparation.RoundPreparationUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roundWaiting.RoundWaitingActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.onlineGame.gameScreen.roundWaiting.RoundWaitingUI


@Composable
fun RowScope.GameScreenActionsUI(
    component: GameScreenComponent
) {
    when (val child = component.childStack.subscribeAsState().value.active.instance) {
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
fun GameScreenUI(
    component: GameScreenComponent
) {
    when (val child = component.childStack.subscribeAsState().value.active.instance) {
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