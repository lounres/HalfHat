package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.RoundScreenComponent
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundEditing.RoundEditingGameCardUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundExplanation.RoundExplanationGameCardUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundLastGuess.RoundLastGuessGameCardUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundPreparation.RoundPreparationGameCardUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundWaiting.RoundWaitingGameCardUI
import dev.lounres.kone.hub.subscribeAsState


@Composable
fun RoundScreenGameCardUI(
    component: RoundScreenComponent,
    modifier: Modifier,
) {
    Card(
        modifier = modifier,
    ) {
        when (val child = component.roundChildSlot.subscribeAsState().value.component) {
            is RoundScreenComponent.RoundChild.RoundWaiting -> RoundWaitingGameCardUI(child.component)
            is RoundScreenComponent.RoundChild.RoundPreparation -> RoundPreparationGameCardUI(child.component)
            is RoundScreenComponent.RoundChild.RoundExplanation -> RoundExplanationGameCardUI(child.component)
            is RoundScreenComponent.RoundChild.RoundLastGuess -> RoundLastGuessGameCardUI(child.component)
            is RoundScreenComponent.RoundChild.RoundEditing -> RoundEditingGameCardUI(child.component)
        }
    }
}