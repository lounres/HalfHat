package dev.lounres.halfhat.client.desktop.ui.utils

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt


internal sealed interface AutoScalingStep {
    data object Initialization: AutoScalingStep
    data class FoundMinimum(val minimum: TextUnit, val ratio: Float) : AutoScalingStep
    data class FoundMaximum(val maximum: TextUnit, val ratio: Float) : AutoScalingStep
    data class FoundSegment(val start: TextUnit, val end: TextUnit, val ratio: Float) : AutoScalingStep
    data object Finished : AutoScalingStep
}

internal fun autoScale(
    textMeasurer: TextMeasurer,
    text: String,
    textStyleToScale: TextStyle,
    constraints: Constraints,
    overflow: TextOverflow,
    softWrap: Boolean,
    maxLines: Int,
    autoScalingAlpha: Float,
    autoScalingGap: Float,
): TextStyle {
    var autoScalingStep: AutoScalingStep = AutoScalingStep.Initialization
    var scaledTextStyle = textStyleToScale
    
    while (true) {
        val textLayoutResult = textMeasurer.measure(
            text = text,
            style = scaledTextStyle,
            constraints = constraints,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
        )
        when (autoScalingStep) {
            is AutoScalingStep.Initialization ->
                if (textLayoutResult.hasVisualOverflow) {
                    autoScalingStep = AutoScalingStep.FoundMaximum(scaledTextStyle.fontSize, autoScalingAlpha)
                    scaledTextStyle = scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize * autoScalingAlpha)
                } else {
                    autoScalingStep = AutoScalingStep.FoundMinimum(scaledTextStyle.fontSize, autoScalingAlpha)
                    scaledTextStyle = scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize / autoScalingAlpha)
                }
            
            is AutoScalingStep.FoundMaximum ->
                if (textLayoutResult.hasVisualOverflow) {
                    val ratio = autoScalingStep.ratio
                    autoScalingStep = AutoScalingStep.FoundMaximum(scaledTextStyle.fontSize, ratio)
                    scaledTextStyle = scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize * ratio)
                } else {
                    val start = scaledTextStyle.fontSize
                    val end = autoScalingStep.maximum
                    val ratio = sqrt(autoScalingStep.ratio)
                    autoScalingStep = AutoScalingStep.FoundSegment(start = start, end = end, ratio = ratio)
                    scaledTextStyle = scaledTextStyle.copy(fontSize = start * ratio)
                }
            
            is AutoScalingStep.FoundMinimum ->
                if (textLayoutResult.hasVisualOverflow) {
                    val start = autoScalingStep.minimum
                    val end = scaledTextStyle.fontSize
                    val ratio = sqrt(autoScalingStep.ratio)
                    autoScalingStep = AutoScalingStep.FoundSegment(start = start, end = end, ratio = ratio)
                    scaledTextStyle = scaledTextStyle.copy(fontSize = start * ratio)
                } else {
                    val ratio = autoScalingStep.ratio
                    autoScalingStep = AutoScalingStep.FoundMinimum(scaledTextStyle.fontSize, ratio)
                    scaledTextStyle = scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize / ratio)
                }
            
            is AutoScalingStep.FoundSegment ->
                if (autoScalingStep.ratio > autoScalingGap) {
                    scaledTextStyle = scaledTextStyle.copy(fontSize = autoScalingStep.start)
                    autoScalingStep = AutoScalingStep.Finished
                } else {
                    val ratio = sqrt(autoScalingStep.ratio)
                    if (textLayoutResult.hasVisualOverflow) {
                        val start = autoScalingStep.start
                        val end = scaledTextStyle.fontSize
                        autoScalingStep = AutoScalingStep.FoundSegment(start = start, end = end, ratio = ratio)
                        scaledTextStyle = scaledTextStyle.copy(fontSize = start * ratio)
                    } else {
                        val start = scaledTextStyle.fontSize
                        val end = autoScalingStep.end
                        autoScalingStep = AutoScalingStep.FoundSegment(start = start, end = end, ratio = ratio)
                        scaledTextStyle = scaledTextStyle.copy(fontSize = end / ratio)
                    }
                }
            
            is AutoScalingStep.Finished -> break
        }
    }
    return scaledTextStyle
}

@Composable
fun AutoScalingText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 16.sp,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    autoScalingAlpha: Float = 0.9f,
    autoScalingGap: Float = 0.99f,
    style: TextStyle = LocalTextStyle.current
) {
    BoxWithConstraints(modifier = modifier) {
        require(0f < autoScalingAlpha && autoScalingAlpha <= autoScalingGap && autoScalingGap < 1f) { "Auto scaling alpha must be between 0 and 1" }
        val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }
        val textMeasurer = rememberTextMeasurer()
        val scaledTextStyle =
            autoScale(
                textMeasurer = textMeasurer,
                text = text,
                textStyleToScale = style.merge(
                    color = textColor,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    textAlign = textAlign ?: TextAlign.Unspecified,
                    lineHeight = lineHeight,
                    fontFamily = fontFamily,
                    textDecoration = textDecoration,
                    fontStyle = fontStyle,
                    letterSpacing = letterSpacing
                ),
                constraints = constraints,
                overflow = overflow,
                softWrap = softWrap,
                maxLines = maxLines,
                autoScalingAlpha = autoScalingAlpha,
                autoScalingGap = autoScalingGap,
            )
        
        BasicText(
            modifier = Modifier.align(Alignment.Center),
            text = text,
            style = scaledTextStyle,
            onTextLayout = onTextLayout,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines
        )
    }
}