package dev.lounres.thetruehat.client.desktop.ui.onlineGame.roundCountdown

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.lounres.thetruehat.client.common.components.onlineGame.roundCountdown.RoundCountdownPageComponent
import dev.lounres.thetruehat.client.desktop.uiTemplates.RoundPageTemplateUI


@Composable
public fun RoundCountdownPageUI(
    component: RoundCountdownPageComponent
) {
    RoundPageTemplateUI(
        backButtonEnabled = component.backButtonEnabled,
        unitsUntilEnd = component.unitsUntilEnd,
        volumeOn = component.volumeOn,
        showFinishButton = component.showFinishButton,
        onBackButtonClick = component.onBackButtonClick,
        onLanguageChange = component.onLanguageChange,
        onFeedbackButtonClick = component.onFeedbackButtonClick,
        onHatButtonClick = component.onHatButtonClick,
        onExitButtonClick = component.onExitButtonClick,
        onVolumeButtonClick = component.onVolumeButtonClick,
        onFinishButtonClick = component.onFinishButtonClick,
    ) {
        Text(
            text = component.countsUntilStart.subscribeAsState().value.toString(),
            fontSize = 130.sp,
        )
    }
}