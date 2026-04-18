package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.gameResults.GameResultsComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.loading.LoadingComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roomGathering.RoomGatheringComponent
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.gameInitialisation.GameInitialisationComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.RoundScreenComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.wordsCollection.WordsCollectionComponent
import dev.lounres.kone.hub.KoneAsynchronousHub


public interface GameScreenComponent {
    public val childSlot: KoneAsynchronousHub<ChildrenSlot<*, Child, UIComponentContext>>
    
    public sealed interface Child {
        public data class Loading(val component: LoadingComponent) : Child
        public data class RoomGathering(val component: RoomGatheringComponent) : Child
        public data class GameInitialisation(val component: GameInitialisationComponent) : Child // TODO
        public data class PlayersWordsCollection(val component: WordsCollectionComponent) : Child
        public data class RoundScreen(val component: RoundScreenComponent) : Child
        public data class GameResults(val component: GameResultsComponent) : Child
    }
}