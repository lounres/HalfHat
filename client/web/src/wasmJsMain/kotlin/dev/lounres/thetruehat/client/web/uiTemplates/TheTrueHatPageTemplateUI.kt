package dev.lounres.thetruehat.client.desktop.uiTemplates

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.client.common.uiComponents.CircleButtonWithIcon
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalResourceApi::class)
@Composable
fun TheTrueHatPageTemplateUI(
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
                    icon = painterResource("icons/translate_black_x2_24dp.png"),
                ) {
                    expanded = !expanded
                }
                CircleButtonWithIcon(
                    icon = painterResource("icons/feedback_black_x2_24dp.png"),
                    onClick = onFeedbackButtonClick,
                )
            }
        }
        pageContent()
    }
}