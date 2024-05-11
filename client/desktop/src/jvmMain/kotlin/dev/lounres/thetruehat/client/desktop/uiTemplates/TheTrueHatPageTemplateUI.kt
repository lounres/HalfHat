package dev.lounres.thetruehat.client.desktop.uiTemplates

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.client.common.resources.Res
import dev.lounres.thetruehat.client.common.resources.feedback_black_x2_24dp
import dev.lounres.thetruehat.client.common.resources.translate_black_x2_24dp
import dev.lounres.thetruehat.client.common.uiComponents.CircleButtonWithIcon
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


//@Preview
//@Composable
//fun TheTrueHatPageTemplateUIPreview() {
//    TheTrueHatPageTemplateUI(
//        backButtonEnabled = true,
//        onBackButtonClick = {},
//        onLanguageChange = {},
//        onFeedbackButtonClick = {},
//        pageHeader = {},
//        pageContent = {},
//    )
//}

@OptIn(ExperimentalResourceApi::class)
@Composable
public fun TheTrueHatPageTemplateUI(
    backButtonEnabled: Boolean,
    onBackButtonClick: () -> Unit,
    onLanguageChange: (Language) -> Unit,
    onFeedbackButtonClick: () -> Unit,
    pageHeader: @Composable RowScope.() -> Unit,
    pageContent: @Composable ColumnScope.() -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (backButtonEnabled) {
                CircleButtonWithIcon(
                    modifier = Modifier.align(Alignment.TopStart),
                    icon = Icons.Default.KeyboardArrowLeft,
                    onClick = onBackButtonClick,
                )
            }
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                pageHeader()
            }
            Row(modifier = Modifier.align(Alignment.TopEnd)) {
                var expanded by remember { mutableStateOf(false) }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Русский") },
                        onClick = {
                            expanded = false
                            onLanguageChange(Language.Russian)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("English") },
                        onClick = {
                            expanded = false
                            onLanguageChange(Language.English)
                        },
                    )
                }
                CircleButtonWithIcon(
                    icon = painterResource(Res.drawable.translate_black_x2_24dp),
                ) {
                    expanded = !expanded
                }
                CircleButtonWithIcon(
                    icon = painterResource(Res.drawable.feedback_black_x2_24dp),
                    onClick = onFeedbackButtonClick,
                )
            }
        }
        HorizontalDivider()
        pageContent()
    }
}