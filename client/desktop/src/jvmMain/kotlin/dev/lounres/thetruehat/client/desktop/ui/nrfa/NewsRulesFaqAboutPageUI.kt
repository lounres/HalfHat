package dev.lounres.thetruehat.client.desktop.ui.nrfa

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.thetruehat.client.common.components.nrfa.FakeNewsRulesFaqAboutPageComponent
import dev.lounres.thetruehat.client.common.components.nrfa.NewsRulesFaqAboutPageComponent
import dev.lounres.thetruehat.client.desktop.uiTemplates.TheTrueHatPageTemplateUI


@Composable
fun NewsRulesFaqAboutSectionHeadUI(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val highlighted by remember { derivedStateOf { enabled || hovered } }
    val color by remember { derivedStateOf { if (highlighted) Color(33, 164, 216) else Color(105, 105, 105) } }
    Text(
        text = text,
        fontSize = 22.sp,
        color = color,
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .drawBehind {
                drawLine(
                    color,
                    Offset(0f, size.height + 5f),
                    Offset(size.width, size.height + 5f),
                    2f
                )
            }
            .clickable(onClick = onClick)
            .hoverable(interactionSource),
    )
}

@Preview
@Composable
fun NewsRulesFaqAboutPageUIPreview() {
    NewsRulesFaqAboutPageUI(
        component = FakeNewsRulesFaqAboutPageComponent()
    )
}

@Composable
fun NewsRulesFaqAboutPageUI(component: NewsRulesFaqAboutPageComponent) {
    TheTrueHatPageTemplateUI(
        backButtonEnabled = component.backButtonEnabled,
        onBackButtonClick = component.onBackButtonClick,
        onLanguageChange = component.onLanguageChange,
        onFeedbackButtonClick = component.onFeedbackButtonClick,
        pageHeader = {
            NewsRulesFaqAboutSectionHeadUI(
                text = "Новости",
                enabled = true,
                onClick = component.onNewsButtonClick,
            )
            NewsRulesFaqAboutSectionHeadUI(
                text = "Правила",
                enabled = false,
                onClick = component.onRulesButtonClick,
            )
            NewsRulesFaqAboutSectionHeadUI(
                text = "FAQ",
                enabled = false,
                onClick = component.onFaqButtonClick,
            )
            NewsRulesFaqAboutSectionHeadUI(
                text = "О нас",
                enabled = false,
                onClick = component.onAboutButtonClick,
            )
        },
        pageContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(390.dp)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(10.dp))
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 10.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                }
            }
        },
    )
}