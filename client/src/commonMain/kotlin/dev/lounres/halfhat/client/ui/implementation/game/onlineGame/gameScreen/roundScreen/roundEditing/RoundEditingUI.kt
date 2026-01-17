package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundEditing

import androidx.compose.foundation.layout.Column
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
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.RoundEditingComponent
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundEditing.listener.RoundEditingListenerContentUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundEditing.player.RoundEditingPlayerContentUI
import dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen.roundEditing.speaker.RoundEditingSpeakerContentUI
import dev.lounres.kone.hub.subscribeAsState


@Composable
fun RoundEditingGameCardUI(
    component: RoundEditingComponent,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Round editing",
            fontWeight = FontWeight.SemiBold,
            autoSize = TextAutoSize.StepBased(maxFontSize = 48.sp),
            softWrap = false,
            maxLines = 1,
        )
        
        when (val child = component.childSlot.subscribeAsState().value.component) {
            is RoundEditingComponent.Child.Speaker -> RoundEditingSpeakerContentUI(child.component)
            is RoundEditingComponent.Child.Listener -> RoundEditingListenerContentUI(child.component)
            is RoundEditingComponent.Child.Player -> RoundEditingPlayerContentUI(child.component)
        }
    }
}