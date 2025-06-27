package dev.lounres.halfhat.client.common.ui.implementation.faq

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.faqPage_dark_png_24dp
import dev.lounres.halfhat.client.common.ui.components.faq.FAQPageComponent
import dev.lounres.halfhat.client.common.ui.components.faq.questionsAndAnswers
import dev.lounres.halfhat.client.common.ui.utils.commonIconModifier
import org.jetbrains.compose.resources.painterResource


@Composable
public fun FAQPageIcon(
    isSelected: Boolean,
) {
    Icon(
        painter = painterResource(Res.drawable.faqPage_dark_png_24dp),
        modifier = commonIconModifier,
        contentDescription = "FAQ page",
    )
}

@Composable
public fun FAQPageBadge(
    component: FAQPageComponent,
    isSelected: Boolean,
) {

}

// TODO: Review "FAQ" page thoroughly
@Composable
public fun FAQPageUI(
    component: FAQPageComponent,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = "Frequently Asked Questions",
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
        )

        for ((question, answer) in questionsAndAnswers)
            Column(
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(
                    text = question,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    fontSize = 16.sp,
                )
            }

        val improveTheGameAnnotatedString = buildAnnotatedString {
            append("Please tell me how to improve the game – I will consider all your comments and suggestions! To do this, go to ")
            withLink(link = LinkAnnotation.Clickable(tag = "feedbackLink") { component.onFeedbackLinkClick() }) {
                withStyle(SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                    append("Feedback page")
                }
            }
            append(".")
        }
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = improveTheGameAnnotatedString,
        )

        val possibleQuestionsAnnotatedString = buildAnnotatedString {
            withStyle(SpanStyle()) {
                append("Still have questions? ")
            }
            append("Ask them ")
            withLink(link = LinkAnnotation.Clickable(tag = "feedbackLink") { component.onFeedbackLinkClick() }) {
                withStyle(SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                    append("here")
                }
            }
            append(".")
        }
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = possibleQuestionsAnnotatedString,
        )
    }
}