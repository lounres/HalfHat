package dev.lounres.halfhat.client.ui.implementation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass
import dev.lounres.halfhat.client.ui.components.MainWindowComponent
import dev.lounres.halfhat.client.ui.components.MainWindowComponentChild
import dev.lounres.halfhat.client.ui.components.MainWindowComponentConfiguration
import dev.lounres.halfhat.client.ui.icons.*
import dev.lounres.halfhat.client.ui.implementation.game.GamePageUI
import dev.lounres.halfhat.client.ui.implementation.home.HomePageUI
import dev.lounres.halfhat.client.ui.implementation.miscellanea.MiscellaneaUI
import dev.lounres.kone.hub.subscribeAsState


@Composable
fun MainWindowPageContentUI(
    modifier: Modifier,
    component: MainWindowComponent,
    windowSizeClass: WindowSizeClass,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when (val openedPage = component.pageVariants.subscribeAsState().value.active.component) {
            is MainWindowComponentChild.Home -> HomePageUI(openedPage.component, windowSizeClass)
            is MainWindowComponentChild.Game -> GamePageUI(openedPage.component, windowSizeClass)
            is MainWindowComponentChild.Miscellanea -> MiscellaneaUI(openedPage.component, windowSizeClass)
        }
    }
}

@Composable
fun MainWindowContentUI(
    component: MainWindowComponent,
    windowSizeClass: WindowSizeClass,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        val minWidthDp = windowSizeClass.minWidthDp
        
        when {
            minWidthDp >= WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND -> // Extra-large width
                Row(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    WideNavigationRail(
                        state = rememberWideNavigationRailState(WideNavigationRailValue.Expanded),
                        arrangement = Arrangement.Center,
                    ) {
                        val selectedConfiguration = component.pageVariants.subscribeAsState().value.active.configuration
                        for (configuration in MainWindowComponentConfiguration.entries) {
                            val selected = configuration == selectedConfiguration
                            WideNavigationRailItem(
                                selected = selected,
                                icon = {
                                    Icon(
                                        imageVector = when (configuration) {
                                            MainWindowComponentConfiguration.Home ->
                                                if (selected) HalfHatIcon.HomePageSelectedIcon
                                                else HalfHatIcon.HomePageIcon
                                            MainWindowComponentConfiguration.Game ->
                                                if (selected) HalfHatIcon.GameModeSelectedIcon
                                                else HalfHatIcon.GameModeIcon
                                            MainWindowComponentConfiguration.Miscellanea ->
                                                HalfHatIcon.MiscellaneaIcon
                                        },
                                        contentDescription = null,
                                    )
                                },
                                label = {
                                    Text(configuration.textName)
                                },
                                railExpanded = true,
                                onClick = { component.openPage(configuration) },
                            )
                        }
                    }
                    MainWindowPageContentUI(
                        modifier = Modifier.fillMaxHeight().weight(1f),
                        component = component,
                        windowSizeClass = windowSizeClass,
                    )
                }
            minWidthDp >= WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND -> // Large width
                Row(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    WideNavigationRail(
                        state = rememberWideNavigationRailState(WideNavigationRailValue.Expanded),
                        arrangement = Arrangement.Center,
                    ) {
                        val selectedConfiguration = component.pageVariants.subscribeAsState().value.active.configuration
                        for (configuration in MainWindowComponentConfiguration.entries) {
                            val selected = configuration == selectedConfiguration
                            WideNavigationRailItem(
                                selected = selected,
                                icon = {
                                    Icon(
                                        imageVector = when (configuration) {
                                            MainWindowComponentConfiguration.Home ->
                                                if (selected) HalfHatIcon.HomePageSelectedIcon
                                                else HalfHatIcon.HomePageIcon
                                            MainWindowComponentConfiguration.Game ->
                                                if (selected) HalfHatIcon.GameModeSelectedIcon
                                                else HalfHatIcon.GameModeIcon
                                            MainWindowComponentConfiguration.Miscellanea ->
                                                HalfHatIcon.MiscellaneaIcon
                                        },
                                        contentDescription = null,
                                    )
                                },
                                label = {
                                    Text(configuration.textName)
                                },
                                railExpanded = true,
                                onClick = { component.openPage(configuration) },
                            )
                        }
                    }
                    MainWindowPageContentUI(
                        modifier = Modifier.fillMaxHeight().weight(1f),
                        component = component,
                        windowSizeClass = windowSizeClass,
                    )
                }
            minWidthDp >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND -> // Expanded width
                Row(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    WideNavigationRail(
                        state = rememberWideNavigationRailState(WideNavigationRailValue.Collapsed),
                        arrangement = Arrangement.Center,
                    ) {
                        val selectedConfiguration = component.pageVariants.subscribeAsState().value.active.configuration
                        for (configuration in MainWindowComponentConfiguration.entries) {
                            val selected = configuration == selectedConfiguration
                            WideNavigationRailItem(
                                selected = selected,
                                icon = {
                                    Icon(
                                        imageVector = when (configuration) {
                                            MainWindowComponentConfiguration.Home ->
                                                if (selected) HalfHatIcon.HomePageSelectedIcon
                                                else HalfHatIcon.HomePageIcon
                                            MainWindowComponentConfiguration.Game ->
                                                if (selected) HalfHatIcon.GameModeSelectedIcon
                                                else HalfHatIcon.GameModeIcon
                                            MainWindowComponentConfiguration.Miscellanea ->
                                                HalfHatIcon.MiscellaneaIcon
                                        },
                                        contentDescription = null,
                                    )
                                },
                                label = {
                                    Text(configuration.textName)
                                },
                                railExpanded = false,
                                onClick = { component.openPage(configuration) },
                            )
                        }
                    }
                    MainWindowPageContentUI(
                        modifier = Modifier.fillMaxHeight().weight(1f),
                        component = component,
                        windowSizeClass = windowSizeClass,
                    )
                }
            minWidthDp >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND -> // Medium width
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    MainWindowPageContentUI(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        component = component,
                        windowSizeClass = windowSizeClass,
                    )
                    NavigationBar {
                        val selectedConfiguration = component.pageVariants.subscribeAsState().value.active.configuration
                        for (configuration in MainWindowComponentConfiguration.entries) {
                            val selected = configuration == selectedConfiguration
                            NavigationBarItem(
                                selected = selected,
                                icon = {
                                    Icon(
                                        imageVector = when (configuration) {
                                            MainWindowComponentConfiguration.Home ->
                                                if (selected) HalfHatIcon.HomePageSelectedIcon
                                                else HalfHatIcon.HomePageIcon
                                            MainWindowComponentConfiguration.Game ->
                                                if (selected) HalfHatIcon.GameModeSelectedIcon
                                                else HalfHatIcon.GameModeIcon
                                            MainWindowComponentConfiguration.Miscellanea ->
                                                HalfHatIcon.MiscellaneaIcon
                                        },
                                        contentDescription = null,
                                    )
                                },
                                label = {
                                    Text(configuration.textName)
                                },
                                onClick = { component.openPage(configuration) },
                            )
                        }
                    }
                }
            else -> // Compact width
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    MainWindowPageContentUI(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        component = component,
                        windowSizeClass = windowSizeClass,
                    )
                    NavigationBar {
                        val selectedConfiguration = component.pageVariants.subscribeAsState().value.active.configuration
                        for (configuration in MainWindowComponentConfiguration.entries) {
                            val selected = configuration == selectedConfiguration
                            NavigationBarItem(
                                selected = selected,
                                icon = {
                                    Icon(
                                        imageVector = when (configuration) {
                                            MainWindowComponentConfiguration.Home ->
                                                if (selected) HalfHatIcon.HomePageSelectedIcon
                                                else HalfHatIcon.HomePageIcon
                                            MainWindowComponentConfiguration.Game ->
                                                if (selected) HalfHatIcon.GameModeSelectedIcon
                                                else HalfHatIcon.GameModeIcon
                                            MainWindowComponentConfiguration.Miscellanea ->
                                                HalfHatIcon.MiscellaneaIcon
                                        },
                                        contentDescription = null,
                                    )
                                },
                                label = {
                                    Text(configuration.textName)
                                },
                                onClick = { component.openPage(configuration) },
                            )
                        }
                    }
                }
        }
    }
}