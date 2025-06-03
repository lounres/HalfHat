package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen

import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.gameResults.GameResultsComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.loading.LoadingComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundEditing.RoundEditingComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundExplanation.RoundExplanationComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundLastGuess.RoundLastGuessComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundPreparation.RoundPreparationComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundWaiting.RoundWaitingComponent
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.kone.state.KoneState


interface GameScreenComponent {
    val onExitGame: () -> Unit
    
    val childStack: KoneState<ChildrenStack<*, Child>>
    
    sealed interface Child {
        class Loading(val component: LoadingComponent) : Child
        class RoundWaiting(val component: RoundWaitingComponent) : Child
        class RoundPreparation(val component: RoundPreparationComponent) : Child
        class RoundExplanation(val component: RoundExplanationComponent) : Child
        class RoundLastGuess(val component: RoundLastGuessComponent) : Child
        class RoundEditing(val component: RoundEditingComponent) : Child
        class GameResults(val component: GameResultsComponent) : Child
    }
}