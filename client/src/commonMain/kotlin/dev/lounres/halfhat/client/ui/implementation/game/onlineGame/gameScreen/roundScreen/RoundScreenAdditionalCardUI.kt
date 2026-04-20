package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.IconToggleButtonShapes
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.RoundScreenComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGamePlayersButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameScheduleButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameSettingsButton
import dev.lounres.halfhat.client.ui.icons.OnlineGameWordsButton
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.list.lastIndex
import dev.lounres.kone.collections.utils.withIndex
import dev.lounres.kone.hub.subscribeAsState
import dev.lounres.kone.maybe.None
import dev.lounres.kone.maybe.Some
import kotlinx.coroutines.launch


private fun ToggleButtonShapes.toIconToggleButtonShapes(): IconToggleButtonShapes =
    IconToggleButtonShapes(
        shape = shape,
        pressedShape = pressedShape,
        checkedShape = checkedShape,
    )

@Composable
fun RoundScreenAdditionalCardUI(
    component: RoundScreenComponent,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            val additionalCardButtons = component.additionalCardButton.subscribeAsState().value
            for ((val index, val button = value) in additionalCardButtons.buttonsList.withIndex()) {
                IconToggleButton(
                    checked = additionalCardButtons.selectedButtonType == button.type,
                    onCheckedChange = {
                        if (it) component.coroutineScope.launch { component.onSelectButton(button) }
                    },
                    shapes = when {
                        additionalCardButtons.buttonsList.size == 1u ->
                            IconButtonDefaults.toggleableShapes()
                        index == 0u ->
                            ButtonGroupDefaults.connectedLeadingButtonShapes().toIconToggleButtonShapes()
                        index == additionalCardButtons.buttonsList.lastIndex ->
                            ButtonGroupDefaults.connectedTrailingButtonShapes().toIconToggleButtonShapes()
                        else ->
                            ButtonGroupDefaults.connectedMiddleButtonShapes().toIconToggleButtonShapes()
                    },
                    colors = IconButtonDefaults.filledTonalIconToggleButtonColors(),
                ) {
                    when (button.type) {
                        RoundScreenComponent.AdditionalCardButton.Type.Schedule ->
                            Icon(
                                modifier = commonIconModifier,
                                imageVector = HalfHatIcon.OnlineGameScheduleButton,
                                contentDescription = "Open schedule",
                            )
                        RoundScreenComponent.AdditionalCardButton.Type.PlayersStatistic ->
                            Icon(
                                modifier = commonIconModifier,
                                imageVector = HalfHatIcon.OnlineGamePlayersButton,
                                contentDescription = "Open players statistic",
                            )
                        RoundScreenComponent.AdditionalCardButton.Type.WordsStatistic ->
                            Icon(
                                modifier = commonIconModifier,
                                imageVector = HalfHatIcon.OnlineGameWordsButton,
                                contentDescription = "Open words statistic",
                            )
                        RoundScreenComponent.AdditionalCardButton.Type.Settings ->
                            Icon(
                                modifier = commonIconModifier,
                                imageVector = HalfHatIcon.OnlineGameSettingsButton,
                                contentDescription = "Open game settings",
                            )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            when (val additionalCardChildMaybe = component.additionalCardChildPossibility.subscribeAsState().value) {
                None -> {

                }
                is Some -> when (val additionalCardChild = additionalCardChildMaybe.value.component) {
                    is RoundScreenComponent.AdditionalCardChild.Schedule ->
                        RoundScreenAdditionalCardScheduleUI(
                            component = component,
                            additionalCardChild = additionalCardChild,
                        )
                    is RoundScreenComponent.AdditionalCardChild.PlayersStatistic ->
                        RoundScreenAdditionalCardPlayersStatisticUI(
                            component = component,
                            additionalCardChild = additionalCardChild,
                        )
                    is RoundScreenComponent.AdditionalCardChild.WordsStatistic ->
                        RoundScreenAdditionalCardWordsStatisticUI(
                            component = component,
                            additionalCardChild = additionalCardChild,
                        )
                    is RoundScreenComponent.AdditionalCardChild.Settings ->
                        RoundScreenAdditionalCardSettingsUI(
                            component = component,
                            additionalCardChild = additionalCardChild,
                        )
                }
            }
        }
    }
}