package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.listener.RoundEditingListenerContentComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.RoundEditingPlayerContentComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.speaker.RoundEditingSpeakerContentComponent
import dev.lounres.kone.hub.KoneAsynchronousHub


public interface RoundEditingComponent {
    public val childSlot: KoneAsynchronousHub<ChildrenSlot<*, Child, UIComponentContext>>
    
    public sealed interface Child {
        public data class Speaker(val component: RoundEditingSpeakerContentComponent) : Child
        public data class Listener(val component: RoundEditingListenerContentComponent) : Child
        public data class Player(val component: RoundEditingPlayerContentComponent) : Child
    }
}