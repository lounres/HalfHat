package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.listener.RoundEditingListenerContentComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.player.RoundEditingPlayerContentComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.speaker.RoundEditingSpeakerContentComponent
import dev.lounres.kone.hub.KoneAsynchronousHub


public interface RoundEditingForPlayerComponent {
    public val childSlot: KoneAsynchronousHub<ChildrenSlot<*, Child, UIComponentContext>>
    
    public sealed interface Child {
        public data class Speaker(val component: RoundEditingSpeakerContentComponent) : Child
        public data class Listener(val component: RoundEditingListenerContentComponent) : Child
        public data class Player(val component: RoundEditingPlayerContentComponent) : Child
    }
}