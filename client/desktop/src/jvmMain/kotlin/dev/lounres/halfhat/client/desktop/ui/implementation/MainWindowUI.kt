package dev.lounres.halfhat.client.desktop.ui.implementation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import dev.lounres.halfhat.Language
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.changeLanguageButton_dark_png_24dp
import dev.lounres.halfhat.client.common.resources.closeMenuButton_dark_png_24dp
import dev.lounres.halfhat.client.common.resources.halfhat_logo
import dev.lounres.halfhat.client.common.resources.openMenuButton_dark
import dev.lounres.halfhat.client.common.resources.volumeOffButton_dark_png_24dp
import dev.lounres.halfhat.client.common.resources.volumeOnButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.MainWindowComponent
import dev.lounres.halfhat.client.common.ui.implementation.about.AboutPageBadge
import dev.lounres.halfhat.client.common.ui.implementation.about.AboutPageIcon
import dev.lounres.halfhat.client.common.ui.implementation.about.AboutPageUI
import dev.lounres.halfhat.client.common.ui.implementation.faq.FAQPageBadge
import dev.lounres.halfhat.client.common.ui.implementation.faq.FAQPageIcon
import dev.lounres.halfhat.client.common.ui.implementation.faq.FAQPageUI
import dev.lounres.halfhat.client.common.ui.implementation.feedback.FeedbackPageBadge
import dev.lounres.halfhat.client.common.ui.implementation.feedback.FeedbackPageIcon
import dev.lounres.halfhat.client.common.ui.implementation.game.GamePageActionsUI
import dev.lounres.halfhat.client.common.ui.implementation.game.GamePageBadge
import dev.lounres.halfhat.client.common.ui.implementation.game.GamePageIcon
import dev.lounres.halfhat.client.common.ui.implementation.game.GamePageUI
import dev.lounres.halfhat.client.common.ui.implementation.gameHistory.GameHistoryPageBadge
import dev.lounres.halfhat.client.common.ui.implementation.gameHistory.GameHistoryPageIcon
import dev.lounres.halfhat.client.common.ui.implementation.home.HomePageBadge
import dev.lounres.halfhat.client.common.ui.implementation.home.HomePageIcon
import dev.lounres.halfhat.client.common.ui.implementation.home.HomePageUI
import dev.lounres.halfhat.client.common.ui.implementation.news.NewsPageBadge
import dev.lounres.halfhat.client.common.ui.implementation.news.NewsPageIcon
import dev.lounres.halfhat.client.common.ui.implementation.rules.RulesPageBadge
import dev.lounres.halfhat.client.common.ui.implementation.rules.RulesPageIcon
import dev.lounres.halfhat.client.common.ui.implementation.settings.SettingsPageBadge
import dev.lounres.halfhat.client.common.ui.implementation.settings.SettingsPageIcon
import dev.lounres.halfhat.client.common.ui.utils.WorkInProgress
import dev.lounres.komponentual.lifecycle.MutableUIComponentLifecycle
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleState
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.state.subscribeAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
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
        TopAppBar(
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
                val volumeOn by component.volumeOn.collectAsState()
                IconButton(
                    onClick = {
                        component.volumeOn.value = !volumeOn
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
                is MainWindowComponent.MenuItem.Child -> {
                    val isSelected = component.pageVariants.subscribeAsState().value.active.configuration == item.child.kind
                    NavigationDrawerItem(
                        selected = isSelected,
                        onClick = {
                            component.openPage(item.child.kind)
                            windowCoroutineScope.launch { drawerState.close() }
                        },
                        icon = {
                            when (val child = item.child) { // TODO: Maybe add child component as a parameter to `*PageIcon` functions below.
                                is MainWindowComponent.Child.Primary.Home -> HomePageIcon(isSelected)
                                is MainWindowComponent.Child.Primary.Game -> GamePageIcon(isSelected)
                                is MainWindowComponent.Child.Secondary.News -> NewsPageIcon(isSelected)
                                is MainWindowComponent.Child.Secondary.Rules -> RulesPageIcon(isSelected)
                                is MainWindowComponent.Child.Secondary.FAQ -> FAQPageIcon(isSelected)
                                is MainWindowComponent.Child.Secondary.GameHistory -> GameHistoryPageIcon(isSelected)
                                is MainWindowComponent.Child.Secondary.Settings -> SettingsPageIcon(isSelected)
                                is MainWindowComponent.Child.Secondary.Feedback -> FeedbackPageIcon(isSelected)
                                is MainWindowComponent.Child.Secondary.About -> AboutPageIcon(isSelected)
                            }
                        },
                        label = {
                            Text(
                                text = item.child.component.textName,
                            )
                        },
                        badge = {
                            when (val child = item.child) {
                                is MainWindowComponent.Child.Primary.Home -> HomePageBadge(child.component, isSelected)
                                is MainWindowComponent.Child.Primary.Game -> GamePageBadge(child.component, isSelected)
                                is MainWindowComponent.Child.Secondary.News -> NewsPageBadge(child.component, isSelected)
                                is MainWindowComponent.Child.Secondary.Rules -> RulesPageBadge(child.component, isSelected)
                                is MainWindowComponent.Child.Secondary.FAQ -> FAQPageBadge(child.component, isSelected)
                                is MainWindowComponent.Child.Secondary.GameHistory -> GameHistoryPageBadge(child.component, isSelected)
                                is MainWindowComponent.Child.Secondary.Settings -> SettingsPageBadge(child.component, isSelected)
                                is MainWindowComponent.Child.Secondary.Feedback -> FeedbackPageBadge(child.component, isSelected)
                                is MainWindowComponent.Child.Secondary.About -> AboutPageBadge(child.component, isSelected)
                            }
                        },
                    )
                }
                
                MainWindowComponent.MenuItem.Separator ->
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
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val pageVariants by component.pageVariants.subscribeAsState()
        val openedPage = pageVariants.active.component
        CenterAlignedTopAppBar(
            navigationIcon = {
                if (windowSizeClass.widthSizeClass <= permanentDrawerAfterWindowWidthSizeClass)IconButton(
                    onClick = {
                        windowCoroutineScope.launch { drawerState.open() }
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.openMenuButton_dark),
                        modifier = Modifier.size(24.dp),
                        contentDescription = "Open menu"
                    )
                }
            },
            title = {
                Text(text = openedPage.component.textName)
            },
            actions = {
                when (openedPage) {
                    is MainWindowComponent.Child.Primary.Home -> {}
                    is MainWindowComponent.Child.Primary.Game -> GamePageActionsUI(openedPage.component)
                    is MainWindowComponent.Child.Secondary.News -> {}
                    is MainWindowComponent.Child.Secondary.Rules -> {}
                    is MainWindowComponent.Child.Secondary.FAQ -> {}
                    is MainWindowComponent.Child.Secondary.GameHistory -> {}
                    is MainWindowComponent.Child.Secondary.Settings -> {}
                    is MainWindowComponent.Child.Secondary.Feedback -> {}
                    is MainWindowComponent.Child.Secondary.About -> {}
                }
            },
        )
        when (openedPage) {
            is MainWindowComponent.Child.Primary.Home -> HomePageUI(openedPage.component)
            is MainWindowComponent.Child.Primary.Game -> GamePageUI(openedPage.component)
            is MainWindowComponent.Child.Secondary.News -> WorkInProgress()
            is MainWindowComponent.Child.Secondary.Rules -> WorkInProgress()
            is MainWindowComponent.Child.Secondary.FAQ -> FAQPageUI(openedPage.component)
            is MainWindowComponent.Child.Secondary.GameHistory -> WorkInProgress()
            is MainWindowComponent.Child.Secondary.Settings -> WorkInProgress()
            is MainWindowComponent.Child.Secondary.Feedback -> WorkInProgress()
            is MainWindowComponent.Child.Secondary.About -> AboutPageUI()
        }
    }
}

@Composable
fun MainWindowContentUI(
    component: MainWindowComponent,
    windowSizeClass: WindowSizeClass,
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
                    
                    val language by component.language.collectAsState()
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
                                        component.language.value = Language.English
                                        openLanguageSelectionDialog.value = false
                                    },
                                    role = Role.RadioButton,
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = language == Language.English,
                                onClick = {
                                    component.language.value = Language.English
                                    openLanguageSelectionDialog.value = false
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
                                        component.language.value = Language.Russian
                                        openLanguageSelectionDialog.value = false
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
                                    component.language.value = Language.Russian
                                    openLanguageSelectionDialog.value = false
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
                ModalDrawerSheet {
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
                PermanentDrawerSheet {
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

@Composable
fun LifecycleController(
    lifecycle: MutableUIComponentLifecycle,
    windowState: WindowState,
    windowInfo: WindowInfo,
) {
    LaunchedEffect(lifecycle, windowState, windowInfo) {
        combine(
            snapshotFlow(windowState::isMinimized),
            snapshotFlow(windowInfo::isWindowFocused),
            ::Pair,
        ).collect { (isMinimized, isFocused) ->
            when {
                isMinimized -> lifecycle.moveTo(UIComponentLifecycleState.Running)
                isFocused -> lifecycle.moveTo(UIComponentLifecycleState.Foreground)
                else -> lifecycle.moveTo(UIComponentLifecycleState.Background)
            }
        }
    }
    
    DisposableEffect(lifecycle) {
        CoroutineScope(Dispatchers.Default).launch {
            lifecycle.moveTo(UIComponentLifecycleState.Running)
        }
        onDispose {
            CoroutineScope(Dispatchers.Default).launch {
                lifecycle.moveTo(UIComponentLifecycleState.Destroyed)
            }
        }
    }
}

@Composable
fun MainWindowUI(
    component: MainWindowComponent?
) {
    if (component != null)
        Window(
            title = "HalfHat — ${component.pageVariants.subscribeAsState().value.active.component.component.textName}",
            icon = painterResource(Res.drawable.halfhat_logo),
            state = component.windowState,
            onCloseRequest = component.onWindowCloseRequest,
        ) {
            LifecycleController(
                component.globalLifecycle,
                component.windowState,
                LocalWindowInfo.current,
            )
            
            var showContent by remember { mutableStateOf(false) }
            
            if (showContent)
                MainWindowContentUI(
                    component = component,
                    windowSizeClass = calculateWindowSizeClass()
                )
            
            LaunchedEffect(Unit) {
                showContent = true
            }
        }
    else
        Window(
            title = "HalfHat",
            icon = painterResource(Res.drawable.halfhat_logo),
            state = rememberWindowState(
                position = WindowPosition.Aligned(Alignment.Center),
                size = DpSize(400.dp, 300.dp)
            ),
            undecorated = true,
            resizable = false,
            onCloseRequest = {},
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Loading...",
                        fontSize = 36.sp,
                    )
                    LoadingIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
}