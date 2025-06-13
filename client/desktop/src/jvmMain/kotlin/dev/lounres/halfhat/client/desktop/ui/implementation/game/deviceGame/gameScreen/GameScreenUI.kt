package dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.gameResults.GameResultsActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.gameResults.GameResultsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.loading.LoadingActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.loading.LoadingUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.roundEditing.RoundEditingActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.roundEditing.RoundEditingUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.roundExplanation.RoundExplanationActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.roundExplanation.RoundExplanationUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.roundLastGuess.RoundLastGuessActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.roundLastGuess.RoundLastGuessUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.roundPreparation.RoundPreparationActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.roundPreparation.RoundPreparationUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.roundWaiting.RoundWaitingActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.deviceGame.gameScreen.roundWaiting.RoundWaitingUI
import dev.lounres.kone.state.subscribeAsState


@Composable
fun RowScope.GameScreenActionsUI(
    component: GameScreenComponent,
) {
    when (val child: GameScreenComponent.Child = component.childStack.subscribeAsState().value.component) {
        is GameScreenComponent.Child.Loading -> LoadingActionsUI(child.component)
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
    component: GameScreenComponent,
) {
    when (val child: GameScreenComponent.Child = component.childStack.subscribeAsState().value.component) {
        is GameScreenComponent.Child.Loading -> LoadingUI(child.component)
        is GameScreenComponent.Child.RoundWaiting -> RoundWaitingUI(child.component)
        is GameScreenComponent.Child.RoundPreparation -> RoundPreparationUI(child.component)
        is GameScreenComponent.Child.RoundExplanation -> RoundExplanationUI(child.component)
        is GameScreenComponent.Child.RoundLastGuess -> RoundLastGuessUI(child.component)
        is GameScreenComponent.Child.RoundEditing -> RoundEditingUI(child.component)
        is GameScreenComponent.Child.GameResults -> GameResultsUI(child.component)
    }
}