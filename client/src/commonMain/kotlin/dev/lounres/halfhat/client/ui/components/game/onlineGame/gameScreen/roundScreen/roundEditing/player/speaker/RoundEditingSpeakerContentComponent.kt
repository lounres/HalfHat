package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.speaker

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import kotlinx.coroutines.flow.StateFlow


interface RoundEditingSpeakerContentComponent {
    public val roundRole: StateFlow<ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Player.RoundRole.Speaker>
    
    public val darkTheme: KoneMutableAsynchronousHub<DarkTheme>
    
    public val onGuessed: (UInt) -> Unit
    public val onNotGuessed: (UInt) -> Unit
    public val onMistake: (UInt) -> Unit
    
    public val onConfirm: () -> Unit
}