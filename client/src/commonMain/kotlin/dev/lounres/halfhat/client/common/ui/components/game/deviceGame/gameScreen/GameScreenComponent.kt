package dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen

import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.gameResults.GameResultsComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.loading.LoadingComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundEditing.RoundEditingComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundExplanation.RoundExplanationComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundLastGuess.RoundLastGuessComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundPreparation.RoundPreparationComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.roundWaiting.RoundWaitingComponent
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.kone.hub.KoneAsynchronousHub


public interface GameScreenComponent {
    public val onExitGame: () -> Unit
    
    public val childSlot: KoneAsynchronousHub<ChildrenSlot<*, Child>>
    
    public sealed interface Child {
        public class Loading(public val component: LoadingComponent) : Child
        public class RoundWaiting(public val component: RoundWaitingComponent) : Child
        public class RoundPreparation(public val component: RoundPreparationComponent) : Child
        public class RoundExplanation(public val component: RoundExplanationComponent) : Child
        public class RoundLastGuess(public val component: RoundLastGuessComponent) : Child
        public class RoundEditing(public val component: RoundEditingComponent) : Child
        public class GameResults(public val component: GameResultsComponent) : Child
    }
}