package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.speaker

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView
import kotlinx.coroutines.flow.StateFlow


interface RoundEditingSpeakerContentComponent {
    public val userRole: StateFlow<ServerApi.OnlineGame.Role.Round.Editing.RoundRole.Speaker>
    
    public val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>
    
    public val onGuessed: (UInt) -> Unit
    public val onNotGuessed: (UInt) -> Unit
    public val onMistake: (UInt) -> Unit
    
    public val onConfirm: () -> Unit
}