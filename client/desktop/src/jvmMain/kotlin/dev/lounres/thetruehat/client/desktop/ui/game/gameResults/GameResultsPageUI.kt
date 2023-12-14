package dev.lounres.thetruehat.client.desktop.ui.game.gameResults

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.thetruehat.client.common.components.game.gameResults.FakeGameResultsPageComponent
import dev.lounres.thetruehat.client.common.components.game.gameResults.GameResultsPageComponent
import dev.lounres.thetruehat.client.common.uiComponents.Table
import dev.lounres.thetruehat.client.desktop.uiTemplates.TheTrueHatPageWithHatTemplateUI


@Preview
@Composable
fun GameResultsPageUIPreview() {
    GameResultsPageUI(
        component = FakeGameResultsPageComponent()
    )
}

@Composable
fun GameResultsPageUI(
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
            Table(
                columnCount = 4,
                rowCount = resultList.size + 1,
            ) { columnIndex, rowIndex ->
                if (rowIndex == 0) {
                    Text(
                        text = when(columnIndex) {
                            0 -> "Игрок"
                            1 -> "Объяснено"
                            2 -> "Угадано"
                            3 -> "Сумма"
                            else -> error(TODO())
                        },
                        fontSize = 15.sp,
                    )
                } else {
                    val result = resultList[rowIndex - 1]
                    Text(
                        text = when(columnIndex) {
                            0 -> result.username
                            1 -> result.scoreExplained.toString()
                            2 -> result.scoreGuessed.toString()
                            3 -> (result.scoreExplained + result.scoreGuessed).toString()
                            else -> error(TODO())
                        },
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}