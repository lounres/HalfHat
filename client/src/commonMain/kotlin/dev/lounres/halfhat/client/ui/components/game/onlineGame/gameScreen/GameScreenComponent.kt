package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.gameResults.GameResultsComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.loading.LoadingComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roomScreen.RoomScreenComponent
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.RoundScreenComponent
import dev.lounres.kone.hub.KoneAsynchronousHubView


public interface GameScreenComponent {
    public val childSlot: KoneAsynchronousHubView<ChildrenSlot<*, Child, UIComponentContext>, *>
    
    public sealed interface Child {
        public data class Loading(val component: LoadingComponent) : Child
        public data class RoomScreen(val component: RoomScreenComponent) : Child
        public data class PlayersWordsCollection(val component: Nothing) : Child
        public data class RoundScreen(val component: RoundScreenComponent) : Child
        public data class GameResults(val component: GameResultsComponent) : Child
    }
}