package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundEditing.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.RoundEditingForPlayerComponent
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundEditing.player.listener.RoundEditingListenerContentUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundEditing.player.player.RoundEditingPlayerContentUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundEditing.player.speaker.RoundEditingSpeakerContentUI
import dev.lounres.kone.hub.subscribeAsState


@Composable
fun ColumnScope.RoundEditingForPlayerGameCardUI(
    component: RoundEditingForPlayerComponent,
) {
    when (val child = component.childSlot.subscribeAsState().value.component) {
        is RoundEditingForPlayerComponent.Child.Speaker -> RoundEditingSpeakerContentUI(child.component)
        is RoundEditingForPlayerComponent.Child.Listener -> RoundEditingListenerContentUI(child.component)
        is RoundEditingForPlayerComponent.Child.Player -> RoundEditingPlayerContentUI(child.component)
    }
}