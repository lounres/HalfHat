package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen

import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.gameResults.GameResultsComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.loading.LoadingComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomScreen.RoomScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomSettings.RoomSettingsComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundEditing.RoundEditingComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundExplanation.RoundExplanationComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundPreparation.RoundPreparationComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundWaiting.RoundWaitingComponent
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.kone.state.KoneState


interface GameScreenComponent {
    val childStack: KoneState<ChildrenStack<*, Child>>
    
    sealed interface Child {
        data class Loading(val component: LoadingComponent) : Child
        data class RoomScreen(val component: RoomScreenComponent) : Child
        data class RoomSettings(val component: RoomSettingsComponent) : Child
        data class RoundWaiting(val component: RoundWaitingComponent) : Child
        data class RoundPreparation(val component: RoundPreparationComponent) : Child
        data class RoundExplanation(val component: RoundExplanationComponent) : Child
        data class RoundLastGuess(val component: RoundLastGuessComponent) : Child
        data class RoundEditing(val component: RoundEditingComponent) : Child
        data class GameResults(val component: GameResultsComponent) : Child
    }
}