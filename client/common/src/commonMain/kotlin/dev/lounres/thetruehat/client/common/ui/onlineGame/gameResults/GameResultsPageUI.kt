package dev.lounres.thetruehat.client.common.ui.onlineGame.gameResults

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.thetruehat.client.common.components.onlineGame.gameResults.GameResultsPageComponent
import dev.lounres.thetruehat.client.common.uiTemplates.TheTrueHatPageWithHatTemplateUI


//@Preview
//@Composable
//fun GameResultsPageUIPreview() {
//    GameResultsPageUI(
//        component = FakeGameResultsPageComponent()
//    )
//}

@Composable
public fun GameResultsPageUI(
    component: GameResultsPageComponent
) {
    TheTrueHatPageWithHatTemplateUI(
        backButtonEnabled = component.backButtonEnabled,
        onBackButtonClick = component.onBackButtonClick,
        onLanguageChange = component.onLanguageChange,
        onFeedbackButtonClick = component.onFeedbackButtonClick,
        onHatButtonClick = component.onHatButtonClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(350.dp)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Результат игры",
                fontSize = 30.sp,
            )

            val resultList = component.resultList

            LazyVerticalGrid(
                modifier = Modifier.fillMaxWidth(),
                columns = GridCells.Fixed(4),
            ) {
                item {
                    Text(
                        text = "Игрок",
                        fontSize = 15.sp,
                    )
                }
                item {
                    Text(
                        text = "Объяснено",
                        fontSize = 15.sp,
                    )
                }
                item {
                    Text(
                        text = "Угадано",
                        fontSize = 15.sp,
                    )
                }
                item {
                    Text(
                        text = "Сумма",
                        fontSize = 15.sp,
                    )
                }
                for (result in resultList) {
                    item {
                        Text(
                            text = result.username,
                            fontSize = 15.sp,
                        )
                    }
                    item {
                        Text(
                            text = result.scoreExplained.toString(),
                            fontSize = 15.sp,
                        )
                    }
                    item {
                        Text(
                            text = result.scoreGuessed.toString(),
                            fontSize = 15.sp,
                        )
                    }
                    item {
                        Text(
                            text = (result.scoreExplained + result.scoreGuessed).toString(),
                            fontSize = 15.sp,
                        )
                    }
                }
            }
        }
    }
}