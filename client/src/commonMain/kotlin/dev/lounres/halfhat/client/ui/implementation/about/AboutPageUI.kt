package dev.lounres.halfhat.client.ui.implementation.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.client.ui.components.about.AboutPageComponent
import dev.lounres.halfhat.client.ui.icons.AboutPageIcon
import dev.lounres.halfhat.client.ui.icons.AboutPageSelectedIcon
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.utils.commonIconModifier


@Composable
public fun AboutPageIcon(
    isSelected: Boolean,
) {
    Icon(
        imageVector = if (isSelected) HalfHatIcon.AboutPageSelectedIcon else HalfHatIcon.AboutPageIcon,
        modifier = commonIconModifier,
        contentDescription = "About page",
    )
}

@Composable
public fun AboutPageBadge(
    component: AboutPageComponent,
    isSelected: Boolean,
) {

}

// TODO: Review "About" page thoroughly
@Composable
public fun AboutPageUI() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold)) {
                    append("HalfHat ")
                }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.outline)) {
                    append("v 0.0.0") // TODO: Add automatic substitution of version
                }
            }
        )
        Spacer(
            modifier = Modifier.height(16.dp)
        )
        val annotatedString = buildAnnotatedString {
            append("The project is developed by Gleb Minaev a.k.a. ")
            withLink(link = LinkAnnotation.Url("https://github.com/lounres/")) {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                    append("@lounres")
                }
            }
            append(". The project's sources are available ")
            withLink(link = LinkAnnotation.Url("https://github.com/lounres/HalfHat")) {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                    append("on GitHub")
                }
            }
            append(".")
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = annotatedString,
        )
    }
}