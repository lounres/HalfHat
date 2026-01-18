package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen

import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.gameResults.GameResultsUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.loading.LoadingUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roomScreen.RoomScreenUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.RoundScreenUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.wordsCollection.WordsCollectionUI
import dev.lounres.kone.hub.subscribeAsState


@Composable
public fun GameScreenUI(
    component: GameScreenComponent,
    windowSizeClass: WindowSizeClass,
) {
    val child = component.childSlot.subscribeAsState().value.component
    when (child) {
        is GameScreenComponent.Child.Loading -> LoadingUI(child.component)
        is GameScreenComponent.Child.RoomScreen -> RoomScreenUI(child.component, windowSizeClass)
        is GameScreenComponent.Child.PlayersWordsCollection -> WordsCollectionUI(child.component, windowSizeClass)
        is GameScreenComponent.Child.RoundScreen -> RoundScreenUI(child.component, windowSizeClass)
        is GameScreenComponent.Child.GameResults -> GameResultsUI(child.component)
    }
}