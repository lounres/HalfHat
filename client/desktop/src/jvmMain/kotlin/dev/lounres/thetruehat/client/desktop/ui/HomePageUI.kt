package dev.lounres.thetruehat.client.desktop.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.thetruehat.client.desktop.components.HomePageComponent
import dev.lounres.thetruehat.client.desktop.components.fake.FakeHomePageComponent


@Preview
@Composable
fun HomePageUIPreview() {
    HomePageUI(
        component = FakeHomePageComponent()
    )
}

@Composable
fun HomePageUI(component: HomePageComponent) {
    TheTrueHatPageWithHatUI(
        component = component.theTrueHatPageWithHatComponent,
        pageContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(350.dp)
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        shape = CircleShape,
                        onClick = component::onCreateButtonClick,
                    ) {
                        Text(
                            text = "Создать",
                            fontSize = 36.sp,
                        )
                    }
                    Button(
                        shape = CircleShape,
                        onClick = component::onEnterButtonClick,
                    ) {
                        Text(
                            text = "Войти",
                            fontSize = 36.sp,
                        )
                    }
                }
            }
        }
    )
}