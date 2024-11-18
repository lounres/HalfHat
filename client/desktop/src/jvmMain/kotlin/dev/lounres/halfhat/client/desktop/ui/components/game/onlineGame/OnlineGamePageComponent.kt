package dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.previewScreen.PreviewScreenComponent


interface OnlineGamePageComponent {
    val childStack: Value<ChildStack<*, Child>>
    
    sealed interface Child {
        data class PreviewScreen(val component: PreviewScreenComponent) : Child
        data class GameScreen(val component: GameScreenComponent) : Child
    }
}