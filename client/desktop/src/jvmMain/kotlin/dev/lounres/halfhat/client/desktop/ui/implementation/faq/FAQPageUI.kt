package dev.lounres.halfhat.client.desktop.ui.implementation.faq

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.desktop.ui.components.faq.questionsAndAnswers


// TODO: Review "FAQ" page thoroughly
@OptIn(ExperimentalTextApi::class)
@Composable
fun FAQPageUI() {
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
            append("Please tell us how to improve the game – I will consider all your comments and suggestions! To do this, go to ")
            withAnnotation(tag = "pageLink", annotation = "Feedback") {
                withStyle(SpanStyle(color = Color.Blue)) {
                    append("Feedback page")
                }
            }
            append(".")
        }
        ClickableText(
            modifier = Modifier.padding(vertical = 16.dp),
            text = improveTheGameAnnotatedString,
            onClick = { offset ->
                improveTheGameAnnotatedString.getStringAnnotations(
                    tag = "pageLink", start = offset, end = offset
                ).forEach {
                    // TODO: Add action for clicking
                }
            },
        )

        val possibleQuestionsAnnotatedString = buildAnnotatedString {
            withStyle(SpanStyle()) {
                append("Still have questions? ")
            }
            append("Ask them ")
            withAnnotation(tag = "pageLink", annotation = "Feedback") {
                withStyle(SpanStyle(color = Color.Blue)) {
                    append("here")
                }
            }
            append(".")
        }
        ClickableText(
            modifier = Modifier.padding(top = 16.dp),
            text = possibleQuestionsAnnotatedString,
            onClick = { offset ->
                possibleQuestionsAnnotatedString.getStringAnnotations(
                    tag = "pageLink", start = offset, end = offset
                ).forEach {
                    // TODO: Add action for clicking
                }
            },
        )
    }
}