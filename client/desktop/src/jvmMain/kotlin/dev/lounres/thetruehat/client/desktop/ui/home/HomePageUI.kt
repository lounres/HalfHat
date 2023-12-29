package dev.lounres.thetruehat.client.desktop.ui.home

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.thetruehat.client.common.components.home.FakeHomePageComponent
import dev.lounres.thetruehat.client.common.components.home.HomePageComponent
import dev.lounres.thetruehat.client.desktop.uiTemplates.TheTrueHatPageWithHatTemplateUI
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


@Preview
@Composable
public fun HomePageUIPreview() {
    HomePageUI(
        component = FakeHomePageComponent()
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
public fun HomePageUI(component: HomePageComponent) {
    TheTrueHatPageWithHatTemplateUI(
        backButtonEnabled = component.backButtonEnabled,
        onBackButtonClick = component.onBackButtonClick,
        onLanguageChange = component.onLanguageChange,
        onFeedbackButtonClick = component.onFeedbackButtonClick,
        onHatButtonClick = component.onHatButtonClick,
        pageContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(IntrinsicSize.Max)
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    onClick = component.onCreateOnlineGameButtonClick,
                ) {
                    Icon(
                        painter = painterResource("icons/internet.png"), // TODO: Fix icons sizes.
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Создать онлайн игру",
                        fontSize = 36.sp,
                        softWrap = false,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    onClick = component.onEnterOnlineGameButtonClick,
                ) {
                    Icon(
                        painter = painterResource("icons/internet.png"),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Войти в онлайн игру",
                        fontSize = 36.sp,
                        softWrap = false,
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    enabled = false,
                    onClick = { TODO() },
                ) {
                    Icon(
                        painter = painterResource("icons/wifi.png"),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Создать локальную игру",
                        fontSize = 36.sp,
                        softWrap = false,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    enabled = false,
                    onClick = { TODO() },
                ) {
                    Icon(
                        painter = painterResource("icons/wifi.png"),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Войти в локальную игру",
                        fontSize = 36.sp,
                        softWrap = false,
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    enabled = false,
                    onClick = { TODO() },
                ) {
                    Icon(
                        painter = painterResource("icons/devices.png"),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Создать игру на устройстве",
                        fontSize = 36.sp,
                        softWrap = false,
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    onClick = component.onCreateGameTimerButtonClick,
                ) {
                    Icon(
                        painter = painterResource("icons/timer.png"),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Создать таймер",
                        fontSize = 36.sp,
                        softWrap = false,
                    )
                }
            }
        }
    )
}