package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.RoundEditingComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundExplanation.RoundExplanationComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundLastGuess.RoundLastGuessComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundPreparation.RoundPreparationComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundWaiting.RoundWaitingComponent
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow


interface RoundScreenComponent {
    public val onExitOnlineGame: () -> Unit
    public val onCopyOnlineGameKey: () -> Unit
    public val onCopyOnlineGameLink: () -> Unit
    public val onFinishGame: () -> Unit
    
    public val gameState: StateFlow<ServerApi.OnlineGame.State.Round>
    
    public val childSlot: KoneAsynchronousHub<ChildrenSlot<*, Child, UIComponentContext>>
    
    public val coroutineScope: CoroutineScope
    public val darkTheme: KoneMutableAsynchronousHub<DarkTheme>
    public val openAdditionalCard: KoneMutableAsynchronousHub<Boolean>
    public val additionalCard: KoneMutableAsynchronousHub<AdditionalCard>
    
    public sealed interface Child {
        public data class RoundWaiting(val component: RoundWaitingComponent) : Child
        public data class RoundPreparation(val component: RoundPreparationComponent) : Child
        public data class RoundExplanation(val component: RoundExplanationComponent) : Child
        public data class RoundLastGuess(val component: RoundLastGuessComponent) : Child
        public data class RoundEditing(val component: RoundEditingComponent) : Child
    }
    
    enum class AdditionalCard {
        Schedule, PlayersStatistic, WordsStatistic, Settings,
    }
}