package dev.lounres.halfhat.client.desktop.ui.implementation

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import dev.lounres.halfhat.api.localization.Language
import dev.lounres.halfhat.client.common.ui.utils.IgnoringMutableInteractionSource
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes
import dev.lounres.halfhat.client.desktop.resources.changeLanguageButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.closeMenuButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.halfhat_logo
import dev.lounres.halfhat.client.desktop.resources.openMenuButton_dark
import dev.lounres.halfhat.client.desktop.resources.volumeOffButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.resources.volumeOnButton_dark_png_24dp
import dev.lounres.halfhat.client.desktop.ui.components.MainWindowComponent
import dev.lounres.kone.collections.KoneList
import dev.lounres.kone.collections.addAllFrom
import dev.lounres.kone.collections.buildKoneList
import dev.lounres.kone.collections.next
import dev.lounres.kone.collections.toKoneList
import dev.lounres.kone.collections.utils.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource


sealed interface MenuItem {
    data object Separator: MenuItem
    data class Page(
        val page: dev.lounres.halfhat.client.desktop.ui.implementation.Page
    ): MenuItem
}

val menuList: KoneList<MenuItem> = buildKoneList {
    addAllFrom(Page.Primary.entries.toKoneList().map { MenuItem.Page(it) })
    add(MenuItem.Separator)
    addAllFrom(Page.Secondary.entries.toKoneList().map { MenuItem.Page(it) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWindowUI(
    component: MainWindowComponent
) {
    val selectedPage by component.selectedPage.collectAsState()
    Window(
        title = "HalfHat — ${selectedPage.textName}",
        icon = painterResource(DesktopRes.drawable.halfhat_logo),
        state = rememberWindowState(
            size = DpSize(360.dp, 640.dp), // Mi Note 3
//            size = DpSize(540.dp, 1200.dp), // POCO X5 Pro 5G
        ),
        onCloseRequest = component.onWindowCloseRequest,
    ) {
        var openLanguageSelectionDialog by remember { mutableStateOf(false) }
        if (openLanguageSelectionDialog)
            Dialog(
                onDismissRequest = {
                    openLanguageSelectionDialog = false
                }
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
                                            openLanguageSelectionDialog = false
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
                                        openLanguageSelectionDialog = false
                                    },
                                    interactionSource = IgnoringMutableInteractionSource,
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
                                            openLanguageSelectionDialog = false
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
                                        openLanguageSelectionDialog = false
                                    },
                                    interactionSource = IgnoringMutableInteractionSource,
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
                                onClick = { openLanguageSelectionDialog = false }
                            ) {
                                Text(text = "Cancel")
                            }
                        }
                    }
                }
            }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val windowCoroutineScope = rememberCoroutineScope()
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        windowCoroutineScope.launch { drawerState.close() }
                                    }
                                ) {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        painter = painterResource(DesktopRes.drawable.closeMenuButton_dark_png_24dp),
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
                                        painter = painterResource(if (volumeOn) DesktopRes.drawable.volumeOnButton_dark_png_24dp else DesktopRes.drawable.volumeOffButton_dark_png_24dp),
                                        modifier = Modifier.size(24.dp),
                                        contentDescription = if (volumeOn) "Volume is on" else "Volume is off"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        openLanguageSelectionDialog = true
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(DesktopRes.drawable.changeLanguageButton_dark_png_24dp),
                                        modifier = Modifier.size(24.dp),
                                        contentDescription = "Choose language of the interface"
                                    )
                                }
                            },
                        )
                        for (item in menuList)
                            when (item) {
                                is MenuItem.Page -> {
                                    val isSelected = selectedPage == item.page
                                    NavigationDrawerItem(
                                        selected = isSelected,
                                        onClick = {
                                            component.selectedPage.value = item.page
                                            windowCoroutineScope.launch { drawerState.close() }
                                        },
                                        icon = { item.page.icon(isSelected) },
                                        label = {
                                            Text(
                                                text = item.page.textName,
                                            )
                                        },
                                        badge = { item.page.badge(component) },
                                    )
                                }

                                MenuItem.Separator ->
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                    )
                            }
                    }
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                windowCoroutineScope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(DesktopRes.drawable.openMenuButton_dark),
                                modifier = Modifier.size(24.dp),
                                contentDescription = "Open menu"
                            )
                        }
                    },
                    title = {
                        Text(text = selectedPage.textName)
                    },
                    actions = { selectedPage.run { actions(component) } },
                )
                selectedPage.content(component)
            }
        }
    }
}