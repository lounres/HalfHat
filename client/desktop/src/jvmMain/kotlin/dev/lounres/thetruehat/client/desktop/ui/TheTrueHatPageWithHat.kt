package dev.lounres.thetruehat.client.desktop.ui

import androidx.compose.runtime.Composable
import dev.lounres.thetruehat.client.common.ui.CircleButtonWithImage
import dev.lounres.thetruehat.client.desktop.components.TheTrueHatPageWithHatComponent
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalResourceApi::class)
@Composable
fun TheTrueHatPageWithHatUI(
    component: TheTrueHatPageWithHatComponent,
    pageContent: @Composable () -> Unit,
) {
    TheTrueHatPageUI(
        component = component.theTrueHatPageComponent,
        pageHeader = {
            CircleButtonWithImage(
                image = painterResource("hat.png"),
                onClick = component::onHatButtonClick,
            )
        },
        pageContent = pageContent,
    )
}