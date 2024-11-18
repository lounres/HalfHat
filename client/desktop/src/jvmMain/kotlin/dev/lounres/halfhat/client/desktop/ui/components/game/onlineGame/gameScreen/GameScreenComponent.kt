package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.gameResults.GameResultsComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.loading.LoadingComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomScreen.RoomScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roomSettings.RoomSettingsComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundEditing.RoundEditingComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundExplanation.RoundExplanationComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundPreparation.RoundPreparationComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.roundWaiting.RoundWaitingComponent


interface GameScreenComponent {
    val childStack: Value<ChildStack<*, Child>>
    
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