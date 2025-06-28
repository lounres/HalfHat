package dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen

import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.gameResults.GameResultsComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.loading.LoadingComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roomScreen.RoomScreenComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roomSettings.RoomSettingsComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundEditing.RoundEditingComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundExplanation.RoundExplanationComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundLastGuess.RoundLastGuessComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundPreparation.RoundPreparationComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.gameScreen.roundWaiting.RoundWaitingComponent
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.kone.state.KoneAsynchronousState
import dev.lounres.kone.state.KoneState


public interface GameScreenComponent {
    public val onExitOnlineGame: () -> Unit
    public val onCopyOnlineGameKey: () -> Unit
    public val onCopyOnlineGameLink: () -> Unit
    
    public val childStack: KoneAsynchronousState<ChildrenStack<*, Child>>
    
    public sealed interface Child {
        public data class Loading(val component: LoadingComponent) : Child
        public data class RoomScreen(val component: RoomScreenComponent) : Child
        public data class RoomSettings(val component: RoomSettingsComponent) : Child
        public data class RoundWaiting(val component: RoundWaitingComponent) : Child
        public data class RoundPreparation(val component: RoundPreparationComponent) : Child
        public data class RoundExplanation(val component: RoundExplanationComponent) : Child
        public data class RoundLastGuess(val component: RoundLastGuessComponent) : Child
        public data class RoundEditing(val component: RoundEditingComponent) : Child
        public data class GameResults(val component: GameResultsComponent) : Child
    }
}