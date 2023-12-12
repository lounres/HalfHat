package dev.lounres.thetruehat.client.desktop.ui.home

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.thetruehat.client.desktop.components.home.HomePageComponent
import dev.lounres.thetruehat.client.desktop.components.home.FakeHomePageComponent
import dev.lounres.thetruehat.client.desktop.uiTemplates.TheTrueHatPageWithHatTemplateUI


@Preview
@Composable
fun HomePageUIPreview() {
    HomePageUI(
        component = FakeHomePageComponent()
    )
}

@Composable
fun HomePageUI(component: HomePageComponent) {
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
                    .width(350.dp)
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    onClick = component.onCreateButtonClick,
                ) {
                    Text(
                        text = "Создать",
                        fontSize = 36.sp,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    onClick = component.onEnterButtonClick,
                ) {
                    Text(
                        text = "Войти",
                        fontSize = 36.sp,
                    )
                }
            }
        }
    )
}