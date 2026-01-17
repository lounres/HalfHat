package dev.lounres.halfhat.client.ui.implementation.miscellanea

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import dev.lounres.halfhat.client.ui.components.miscellanea.MiscellaneaComponent
import dev.lounres.halfhat.client.ui.icons.AboutPageIcon
import dev.lounres.halfhat.client.ui.icons.ChangeLanguageButton
import dev.lounres.halfhat.client.ui.icons.FaqPageIcon
import dev.lounres.halfhat.client.ui.icons.FeedbackPageIcon
import dev.lounres.halfhat.client.ui.icons.GameHistoryIcon
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.NewsPageIcon
import dev.lounres.halfhat.client.ui.icons.RulesPageIcon
import dev.lounres.halfhat.client.ui.icons.SettingsPageIcon
import dev.lounres.halfhat.client.ui.icons.ThemeAutoButton
import dev.lounres.halfhat.client.ui.icons.ThemeDarkButton
import dev.lounres.halfhat.client.ui.icons.ThemeLightButton
import dev.lounres.halfhat.client.ui.icons.VolumeOffButton
import dev.lounres.halfhat.client.ui.icons.VolumeOnButton
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.hub.set
import dev.lounres.kone.hub.subscribeAsState
import kotlinx.coroutines.launch


@Composable
fun MiscellaneaUI(
    component: MiscellaneaComponent,
    windowSizeClass: WindowSizeClass,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                val coroutineScope = rememberCoroutineScope()
                
                val darkTheme by component.darkTheme.subscribeAsState()
                FilledIconButton(
                    onClick = {
                        coroutineScope.launch {
                            component.darkTheme.set(DarkTheme.entries[(darkTheme.ordinal + 1) % DarkTheme.entries.size])
                        }
                    }
                ) {
                    Icon(
                        imageVector = when (darkTheme) {
                            DarkTheme.Enabled -> HalfHatIcon.ThemeDarkButton
                            DarkTheme.System -> HalfHatIcon.ThemeAutoButton
                            DarkTheme.Disabled -> HalfHatIcon.ThemeLightButton
                        },
                        modifier = Modifier.size(24.dp),
                        contentDescription = "Switch dark theme mode"
                    )
                }

                val volumeOn by component.volumeOn.subscribeAsState()
                FilledIconButton(
                    onClick = {
                        coroutineScope.launch {
                            component.volumeOn.set(!volumeOn)
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (volumeOn) HalfHatIcon.VolumeOnButton else HalfHatIcon.VolumeOffButton,
                        modifier = Modifier.size(24.dp),
                        contentDescription = if (volumeOn) "Volume is on" else "Volume is off"
                    )
                }
                
                FilledIconButton(
                    enabled = false,
                    onClick = {
//                        openLanguageSelectionDialog.value = true // TODO
                    }
                ) {
                    Icon(
                        imageVector = HalfHatIcon.ChangeLanguageButton,
                        modifier = Modifier.size(24.dp),
                        contentDescription = "Choose language of the interface"
                    )
                }
            }
            BoxWithConstraints(
                modifier = Modifier.fillMaxHeight().weight(1f).verticalScroll(rememberScrollState()),
            ) {
                val maxWidth = maxWidth
                FlowRow(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Card(
                        modifier = Modifier
                            .widthIn(max = maxWidth)
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(4.dp),
                        enabled = false,
                        onClick = component.openSettings
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                modifier = Modifier.size(96.dp),
                                imageVector = HalfHatIcon.SettingsPageIcon,
                                contentDescription = null,
                            )
                            Text(
                                text = "Settings",
                            )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .widthIn(max = maxWidth / 2)
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(4.dp),
                        enabled = false,
                        onClick = component.openGameHistory
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                modifier = Modifier.size(96.dp),
                                imageVector = HalfHatIcon.GameHistoryIcon,
                                contentDescription = null,
                            )
                            Text(
                                text = "Game history",
                            )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .widthIn(max = maxWidth / 2)
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(4.dp),
                        enabled = false,
                        onClick = component.openFeedback
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                modifier = Modifier.size(96.dp),
                                imageVector = HalfHatIcon.FeedbackPageIcon,
                                contentDescription = null,
                            )
                            Text(
                                text = "Feedback",
                            )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .widthIn(max = maxWidth / 2)
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(4.dp),
                        enabled = false,
                        onClick = component.openRules,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                modifier = Modifier.size(96.dp),
                                imageVector = HalfHatIcon.RulesPageIcon,
                                contentDescription = null,
                            )
                            Text(
                                text = "Rules",
                            )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .widthIn(max = maxWidth / 2)
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(4.dp),
                        enabled = false,
                        onClick = component.openFAQ,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                modifier = Modifier.size(96.dp),
                                imageVector = HalfHatIcon.FaqPageIcon,
                                contentDescription = null,
                            )
                            Text(
                                text = "FAQ",
                            )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .widthIn(max = maxWidth / 2)
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(4.dp),
                        enabled = false,
                        onClick = component.openAbout,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                modifier = Modifier.size(96.dp),
                                imageVector = HalfHatIcon.AboutPageIcon,
                                contentDescription = null,
                            )
                            Text(
                                text = "About",
                            )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .widthIn(max = maxWidth / 2)
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(4.dp),
                        enabled = false,
                        onClick = component.openNews,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                modifier = Modifier.size(96.dp),
                                imageVector = HalfHatIcon.NewsPageIcon,
                                contentDescription = null,
                            )
                            Text(
                                text = "News",
                            )
                        }
                    }
                }
            }
        }
    }
}