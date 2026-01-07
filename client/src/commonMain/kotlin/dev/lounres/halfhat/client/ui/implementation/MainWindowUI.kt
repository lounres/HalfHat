package dev.lounres.halfhat.client.ui.implementation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.lounres.halfhat.Language
import dev.lounres.halfhat.client.resources.Res
import dev.lounres.halfhat.client.resources.changeLanguageButton_dark_png_24dp
import dev.lounres.halfhat.client.resources.closeMenuButton_dark_png_24dp
import dev.lounres.halfhat.client.resources.darkThemeButton_dark_png_24dp
import dev.lounres.halfhat.client.resources.lightThemeButton_dark_png_24dp
import dev.lounres.halfhat.client.resources.openMenuButton_dark
import dev.lounres.halfhat.client.resources.systemThemeButton_dark_png_24dp
import dev.lounres.halfhat.client.resources.volumeOffButton_dark_png_24dp
import dev.lounres.halfhat.client.resources.volumeOnButton_dark_png_24dp
import dev.lounres.halfhat.client.ui.components.MainWindowComponent
import dev.lounres.halfhat.client.ui.components.MainWindowComponentChild
import dev.lounres.halfhat.client.ui.components.MainWindowComponentMenuItem
import dev.lounres.halfhat.client.ui.implementation.about.AboutPageBadge
import dev.lounres.halfhat.client.ui.implementation.about.AboutPageIcon
import dev.lounres.halfhat.client.ui.implementation.about.AboutPageUI
import dev.lounres.halfhat.client.ui.implementation.faq.FAQPageBadge
import dev.lounres.halfhat.client.ui.implementation.faq.FAQPageIcon
import dev.lounres.halfhat.client.ui.implementation.faq.FAQPageUI
import dev.lounres.halfhat.client.ui.implementation.feedback.FeedbackPageBadge
import dev.lounres.halfhat.client.ui.implementation.feedback.FeedbackPageIcon
import dev.lounres.halfhat.client.ui.implementation.game.GamePageActionsUI
import dev.lounres.halfhat.client.ui.implementation.game.GamePageBadge
import dev.lounres.halfhat.client.ui.implementation.game.GamePageIcon
import dev.lounres.halfhat.client.ui.implementation.game.GamePageUI
import dev.lounres.halfhat.client.ui.implementation.gameHistory.GameHistoryPageBadge
import dev.lounres.halfhat.client.ui.implementation.gameHistory.GameHistoryPageIcon
import dev.lounres.halfhat.client.ui.implementation.home.HomePageBadge
import dev.lounres.halfhat.client.ui.implementation.home.HomePageIcon
import dev.lounres.halfhat.client.ui.implementation.home.HomePageUI
import dev.lounres.halfhat.client.ui.implementation.news.NewsPageBadge
import dev.lounres.halfhat.client.ui.implementation.news.NewsPageIcon
import dev.lounres.halfhat.client.ui.implementation.rules.RulesPageBadge
import dev.lounres.halfhat.client.ui.implementation.rules.RulesPageIcon
import dev.lounres.halfhat.client.ui.implementation.settings.SettingsPageBadge
import dev.lounres.halfhat.client.ui.implementation.settings.SettingsPageIcon
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.client.ui.utils.WorkInProgress
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.hub.set
import dev.lounres.kone.hub.subscribeAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource


val permanentDrawerAfterWindowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Medium

@Composable
fun MainWindowDrawerSheetContentUI(
    component: MainWindowComponent,
    windowSizeClass: WindowSizeClass,
    openLanguageSelectionDialog: MutableState<Boolean>,
    windowCoroutineScope: CoroutineScope,
    drawerState: DrawerState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        val coroutineScope = rememberCoroutineScope()
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
            navigationIcon = {
                if (windowSizeClass.widthSizeClass <= permanentDrawerAfterWindowWidthSizeClass)
                    IconButton(
                        onClick = {
                            windowCoroutineScope.launch { drawerState.close() }
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(Res.drawable.closeMenuButton_dark_png_24dp),
                            contentDescription = "Close menu"
                        )
                    }
            },
            title = {},
            actions = {
                val darkTheme by component.darkTheme.subscribeAsState()
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            component.darkTheme.set(DarkTheme.entries[(darkTheme.ordinal + 1) % DarkTheme.entries.size])
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            when (darkTheme) {
                                DarkTheme.Enabled -> Res.drawable.darkThemeButton_dark_png_24dp
                                DarkTheme.System -> Res.drawable.systemThemeButton_dark_png_24dp
                                DarkTheme.Disabled -> Res.drawable.lightThemeButton_dark_png_24dp
                            }
                        ),
                        modifier = Modifier.size(24.dp),
                        contentDescription = "Switch dark theme mode"
                    )
                }
                val volumeOn by component.volumeOn.subscribeAsState()
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            component.volumeOn.set(!volumeOn)
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(if (volumeOn) Res.drawable.volumeOnButton_dark_png_24dp else Res.drawable.volumeOffButton_dark_png_24dp),
                        modifier = Modifier.size(24.dp),
                        contentDescription = if (volumeOn) "Volume is on" else "Volume is off"
                    )
                }
                IconButton(
                    onClick = {
                        openLanguageSelectionDialog.value = true
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.changeLanguageButton_dark_png_24dp),
                        modifier = Modifier.size(24.dp),
                        contentDescription = "Choose language of the interface"
                    )
                }
            },
        )
        for (item in component.menuList.subscribeAsState().value)
            when (item) {
                is MainWindowComponentMenuItem.Child -> {
                    val isSelected = component.pageVariants.subscribeAsState().value.active.configuration == item.child.kind
                    NavigationDrawerItem(
                        selected = isSelected,
                        onClick = {
                            component.openPage(item.child.kind)
                            windowCoroutineScope.launch { drawerState.close() }
                        },
                        icon = {
                            when (val child = item.child) { // TODO: Maybe add child component as a parameter to `*PageIcon` functions below.
                                is MainWindowComponentChild.Primary.Home -> HomePageIcon(isSelected)
                                is MainWindowComponentChild.Primary.Game -> GamePageIcon(isSelected)
                                is MainWindowComponentChild.Secondary.News -> NewsPageIcon(isSelected)
                                is MainWindowComponentChild.Secondary.Rules -> RulesPageIcon(isSelected)
                                is MainWindowComponentChild.Secondary.FAQ -> FAQPageIcon(isSelected)
                                is MainWindowComponentChild.Secondary.GameHistory -> GameHistoryPageIcon(isSelected)
                                is MainWindowComponentChild.Secondary.Settings -> SettingsPageIcon(isSelected)
                                is MainWindowComponentChild.Secondary.Feedback -> FeedbackPageIcon(isSelected)
                                is MainWindowComponentChild.Secondary.About -> AboutPageIcon(isSelected)
                            }
                        },
                        label = {
                            Text(
                                text = item.child.component.textName,
                            )
                        },
                        badge = {
                            when (val child = item.child) {
                                is MainWindowComponentChild.Primary.Home -> HomePageBadge(child.component, isSelected)
                                is MainWindowComponentChild.Primary.Game -> GamePageBadge(child.component, isSelected)
                                is MainWindowComponentChild.Secondary.News -> NewsPageBadge(child.component, isSelected)
                                is MainWindowComponentChild.Secondary.Rules -> RulesPageBadge(child.component, isSelected)
                                is MainWindowComponentChild.Secondary.FAQ -> FAQPageBadge(child.component, isSelected)
                                is MainWindowComponentChild.Secondary.GameHistory -> GameHistoryPageBadge(child.component, isSelected)
                                is MainWindowComponentChild.Secondary.Settings -> SettingsPageBadge(child.component, isSelected)
                                is MainWindowComponentChild.Secondary.Feedback -> FeedbackPageBadge(child.component, isSelected)
                                is MainWindowComponentChild.Secondary.About -> AboutPageBadge(child.component, isSelected)
                            }
                        },
                    )
                }

                MainWindowComponentMenuItem.Separator ->
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
            }
    }
}

@Composable
fun MainWindowDrawerContentUI(
    component: MainWindowComponent,
    windowSizeClass: WindowSizeClass,
    windowCoroutineScope: CoroutineScope,
    drawerState: DrawerState,
) {
    Surface {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val pageVariants by component.pageVariants.subscribeAsState()
            val openedPage = pageVariants.active.component
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                navigationIcon = {
                    if (windowSizeClass.widthSizeClass <= permanentDrawerAfterWindowWidthSizeClass) IconButton(
                        onClick = {
                            windowCoroutineScope.launch { drawerState.open() }
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.openMenuButton_dark),
                            modifier = commonIconModifier,
                            contentDescription = "Open menu"
                        )
                    }
                },
                title = {
                    Text(text = openedPage.component.textName)
                },
                actions = {
                    when (openedPage) {
                        is MainWindowComponentChild.Primary.Home -> {}
                        is MainWindowComponentChild.Primary.Game -> GamePageActionsUI(openedPage.component)
                        is MainWindowComponentChild.Secondary.News -> {}
                        is MainWindowComponentChild.Secondary.Rules -> {}
                        is MainWindowComponentChild.Secondary.FAQ -> {}
                        is MainWindowComponentChild.Secondary.GameHistory -> {}
                        is MainWindowComponentChild.Secondary.Settings -> {}
                        is MainWindowComponentChild.Secondary.Feedback -> {}
                        is MainWindowComponentChild.Secondary.About -> {}
                    }
                },
            )
            when (openedPage) {
                is MainWindowComponentChild.Primary.Home -> HomePageUI(openedPage.component)
                is MainWindowComponentChild.Primary.Game -> GamePageUI(openedPage.component)
                is MainWindowComponentChild.Secondary.News -> WorkInProgress()
                is MainWindowComponentChild.Secondary.Rules -> WorkInProgress()
                is MainWindowComponentChild.Secondary.FAQ -> FAQPageUI(openedPage.component)
                is MainWindowComponentChild.Secondary.GameHistory -> WorkInProgress()
                is MainWindowComponentChild.Secondary.Settings -> WorkInProgress()
                is MainWindowComponentChild.Secondary.Feedback -> WorkInProgress()
                is MainWindowComponentChild.Secondary.About -> AboutPageUI()
            }
        }
    }
}

@Composable
fun MainWindowContentUI(
    component: MainWindowComponent,
    windowSizeClass: WindowSizeClass,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        val openLanguageSelectionDialog = remember { mutableStateOf(false) }
        if (openLanguageSelectionDialog.value)
            Dialog(
                onDismissRequest = {
                    openLanguageSelectionDialog.value = false
                },
            ) {
                Card(
                    modifier = Modifier,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            text = "Choose language",
                            fontSize = 24.sp,
                        )

                        val coroutineScope = rememberCoroutineScope()
                        val language by component.language.subscribeAsState()
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    .toggleable(
                                        value = language == Language.English,
                                        onValueChange = {
                                            coroutineScope.launch {
                                                component.language.set(Language.English)
                                                openLanguageSelectionDialog.value = false
                                            }
                                        },
                                        role = Role.RadioButton,
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = language == Language.English,
                                    onClick = {
                                        coroutineScope.launch {
                                            component.language.set(Language.English)
                                            openLanguageSelectionDialog.value = false
                                        }
                                    },
                                )
                                Text(text = "English")
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    .toggleable(
                                        enabled = false,
                                        value = language == Language.Russian,
                                        onValueChange = {
                                            coroutineScope.launch {
                                                component.language.set(Language.Russian)
                                                openLanguageSelectionDialog.value = false
                                            }
                                        },
                                        role = Role.RadioButton,
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    enabled = false,
                                    selected = language == Language.Russian,
                                    onClick = {
                                        coroutineScope.launch {
                                            component.language.set(Language.Russian)
                                            openLanguageSelectionDialog.value = false
                                        }
                                    },
                                )
                                Text(text = "Русский")
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { openLanguageSelectionDialog.value = false }
                            ) {
                                Text(text = "Cancel")
                            }
                        }
                    }
                }
            }

        val windowCoroutineScope = rememberCoroutineScope()
        if (windowSizeClass.widthSizeClass <= permanentDrawerAfterWindowWidthSizeClass) {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        MainWindowDrawerSheetContentUI(
                            component = component,
                            windowSizeClass = windowSizeClass,
                            openLanguageSelectionDialog = openLanguageSelectionDialog,
                            windowCoroutineScope = windowCoroutineScope,
                            drawerState = drawerState,
                        )
                    }
                }
            ) {
                MainWindowDrawerContentUI(
                    component = component,
                    windowSizeClass = windowSizeClass,
                    windowCoroutineScope = windowCoroutineScope,
                    drawerState = drawerState,
                )
            }
        } else {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        MainWindowDrawerSheetContentUI(
                            component = component,
                            windowSizeClass = windowSizeClass,
                            openLanguageSelectionDialog = openLanguageSelectionDialog,
                            windowCoroutineScope = windowCoroutineScope,
                            drawerState = drawerState,
                        )
                    }
                }
            ) {
                MainWindowDrawerContentUI(
                    component = component,
                    windowSizeClass = windowSizeClass,
                    windowCoroutineScope = windowCoroutineScope,
                    drawerState = drawerState,
                )
            }
        }
    }
}