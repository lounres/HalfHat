package dev.lounres.thetruehat.client.desktop.ui.deviceGame

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.thetruehat.client.desktop.uiTemplates.TheTrueHatPageWithHatTemplateUI
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalResourceApi::class)
@Preview
@Composable
fun RoomPage() {
    TheTrueHatPageWithHatTemplateUI(
        backButtonEnabled = true,
        onBackButtonClick = { TODO() },
        onLanguageChange = { TODO() },
        onFeedbackButtonClick = { TODO() },
        onHatButtonClick = { TODO() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(450.dp)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(
                    onClick = { TODO() }
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                    )
                }
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    val state = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        state = state,
                    ) {
                        items(listOf("Panther", "Jaguar", "Tiger", "Lion")) { name ->
                            Row(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .height(IntrinsicSize.Max)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxHeight(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = name
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxHeight(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    IconButton(
                                        onClick = { TODO() }
                                    ) {
                                        Icon(
                                            painter = painterResource("icons/words.png"),
                                            contentDescription = null,
                                        )
                                    }
                                    IconButton(
                                        onClick = { TODO() }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                        )
                                    }
                                }
                            }
                            Divider()
                        }
                        item {

                        }
                    }
                    VerticalScrollbar(
                        modifier = Modifier.fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(scrollState = state)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 50.dp),
                    onClick = { TODO() }
                ) {
                    Text(
                        text = "Начать игру",
                        fontSize = 25.sp,
                    )
                }
            }
        }
    }
}