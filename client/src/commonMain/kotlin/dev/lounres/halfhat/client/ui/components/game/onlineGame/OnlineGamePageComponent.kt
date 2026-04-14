package dev.lounres.halfhat.client.ui.components.game.onlineGame

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.GameScreenComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.previewScreen.PreviewScreenComponent
import dev.lounres.kone.hub.KoneAsynchronousHub


public interface OnlineGamePageComponent {
    public val childSlot: KoneAsynchronousHub<ChildrenSlot<*, Child, UIComponentContext>>
    
    public sealed interface Child {
        public data class PreviewScreen(val component: PreviewScreenComponent) : Child
        public data class GameScreen(val component: GameScreenComponent) : Child
    }
}