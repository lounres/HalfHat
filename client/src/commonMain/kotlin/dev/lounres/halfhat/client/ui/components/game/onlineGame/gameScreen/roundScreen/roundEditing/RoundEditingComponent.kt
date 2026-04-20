package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.RoundEditingForPlayerComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.spectator.RoundEditingForSpectatorComponent
import dev.lounres.kone.hub.KoneAsynchronousHub


public interface RoundEditingComponent {
    public val childSlot: KoneAsynchronousHub<ChildrenSlot<*, Child, UIComponentContext>>
    
    public sealed interface Child {
        public data class Player(val component: RoundEditingForPlayerComponent) : Child
        public data class Spectator(val component: RoundEditingForSpectatorComponent) : Child
    }
}