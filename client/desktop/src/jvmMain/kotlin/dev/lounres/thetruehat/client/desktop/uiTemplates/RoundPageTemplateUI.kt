package dev.lounres.thetruehat.client.desktop.uiTemplates

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.UserGameState
import dev.lounres.thetruehat.client.common.resources.Res
import dev.lounres.thetruehat.client.common.resources.exit_black_x1_24dp
import dev.lounres.thetruehat.client.common.resources.volume_off_black_x1_24dp
import dev.lounres.thetruehat.client.common.resources.volume_on_black_x1_24dp
import dev.lounres.thetruehat.client.common.uiComponents.CircleButtonWithIcon
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


//@Preview
//@Composable
//fun RoundPageTemplateUIPreview() {
//
//}

@OptIn(ExperimentalResourceApi::class)
@Composable
public fun RoundPageTemplateUI(
    backButtonEnabled: Boolean,
    unitsUntilEnd: Value<UserGameState.UnitsUntilEnd>,
    volumeOn: Value<Boolean>,
    showFinishButton: Value<Boolean>,
    onBackButtonClick: () -> Unit,
    onLanguageChange: (Language) -> Unit,
    onFeedbackButtonClick: () -> Unit,
    onHatButtonClick: () -> Unit,
    onExitButtonClick: () -> Unit,
    onVolumeButtonClick: () -> Unit,
    onFinishButtonClick: () -> Unit,
    pageContent: @Composable (ColumnScope.() -> Unit),
) {
    TheTrueHatPageWithHatTemplateUI(
        backButtonEnabled = backButtonEnabled,
        onBackButtonClick = onBackButtonClick,
        onLanguageChange = onLanguageChange,
        onFeedbackButtonClick = onFeedbackButtonClick,
        onHatButtonClick = onHatButtonClick,
        pageContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(350.dp)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (val theUnits = unitsUntilEnd.subscribeAsState().value) {
                        is UserGameState.UnitsUntilEnd.Words -> "${theUnits.wordsLeft} слов"
                        is UserGameState.UnitsUntilEnd.Rounds -> "${theUnits.roundsLeft} кругов"
                    },
                    fontSize = 30.sp,
                )
                Row {
                    CircleButtonWithIcon(
                        icon = painterResource(Res.drawable.exit_black_x1_24dp),
                        onClick = onExitButtonClick,
                    )
                    CircleButtonWithIcon(
                        icon = painterResource(if (volumeOn.subscribeAsState().value) Res.drawable.volume_on_black_x1_24dp else Res.drawable.volume_off_black_x1_24dp),
                        onClick = onVolumeButtonClick
                    )
                    if (showFinishButton.subscribeAsState().value)
                        CircleButtonWithIcon(
                            icon = painterResource("icons/finish_black_x1_24dp.png"),
                            onClick = onFinishButtonClick,
                        )
                }
                Spacer(modifier = Modifier.height(10.dp))
                pageContent()
            }
        }
    )
}