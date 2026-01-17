package dev.lounres.halfhat.client.ui.components

import dev.lounres.halfhat.Language
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleKey
import dev.lounres.halfhat.client.components.logger.LoggerKey
import dev.lounres.halfhat.client.components.navigation.ChildrenVariants
import dev.lounres.halfhat.client.components.navigation.NavigationControllerSpec
import dev.lounres.halfhat.client.components.navigation.VariantsNode
import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodePath
import dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot
import dev.lounres.halfhat.client.components.navigation.controller.navigationController
import dev.lounres.halfhat.client.components.navigation.controller.setUpNavigationControl
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultVariantsNode
import dev.lounres.halfhat.client.logic.settings.CustomSounds
import dev.lounres.halfhat.client.logic.settings.DeviceGameDefaultSettingsKey
import dev.lounres.halfhat.client.logic.settings.LanguageKey
import dev.lounres.halfhat.client.logic.settings.VolumeOnKey
import dev.lounres.halfhat.client.logic.settings.volumeOn
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderID
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderRegistry
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderRegistryKey
import dev.lounres.halfhat.client.storage.settings.Settings
import dev.lounres.halfhat.client.storage.settings.SettingsSerializer
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.halfhat.client.ui.components.game.RealGamePageComponent
import dev.lounres.halfhat.client.ui.components.home.RealHomePageComponent
import dev.lounres.halfhat.client.ui.components.miscellanea.RealMiscellaneaComponent
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.collections.interop.toKoneList
import dev.lounres.kone.collections.iterables.isEmpty
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.addAllFrom
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.map.empty
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.set.toKoneSet
import dev.lounres.kone.collections.utils.drop
import dev.lounres.kone.collections.utils.firstThatOrNull
import dev.lounres.kone.hub.KoneAsynchronousHubView
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView
import dev.lounres.kone.hub.value
import dev.lounres.kone.registry.correspondsTo
import dev.lounres.kone.registry.serialization.RegistrySerializableKey
import dev.lounres.logKube.core.CurrentPlatformLogger
import dev.lounres.logKube.core.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


expect class RealMainWindowComponent: MainWindowComponent {
    override val globalLifecycle: MutableUIComponentLifecycle
    
    override val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>
    
    override val pageVariants: KoneAsynchronousHubView<ChildrenVariants<MainWindowComponentConfiguration, MainWindowComponentChild, UIComponentContext>, *>
    override val openPage: (page: MainWindowComponentConfiguration) -> Unit
}

data class SettingDescription<T>(
    val key: RegistrySerializableKey<T>,
    val value: T,
)

expect val defaultDeviceGameWordsSource: GameStateMachine.WordsSource<DeviceGameWordsProviderID>

val defaultDarkThemeMode: DarkTheme = DarkTheme.System

val settingsDefaults: Map<String, SettingDescription<*>> = mapOf(
    "DarkTheme" to SettingDescription(DarkTheme.Key, defaultDarkThemeMode),
    "VolumeOn" to SettingDescription(VolumeOnKey, true),
    "Language" to SettingDescription(LanguageKey, Language.English),
    "InitialSelectedPage" to SettingDescription(MainWindowComponentConfiguration.Key, MainWindowComponentConfiguration.Game),
    "DeviceGameDefaultSettings" to SettingDescription(
        DeviceGameDefaultSettingsKey,
        GameStateMachine.GameSettings.Builder<DeviceGameWordsProviderID>(
            preparationTimeSeconds = 3u,
            explanationTimeSeconds = 20u,
            finalGuessTimeSeconds = 3u,
            strictMode = false,
            cachedEndConditionWordsNumber = 100u,
            cachedEndConditionCyclesNumber = 10u,
            gameEndConditionType = GameStateMachine.GameEndCondition.Type.Words,
            wordsSource = defaultDeviceGameWordsSource,
        )
    ),
    "CustomSounds" to SettingDescription(
        CustomSounds.Key,
        CustomSounds(
            preparationCountdown = null,
            explanationStart = null,
            finalGuessStart = null,
            finalGuessEnd = null,
        )
    ),
)

val settingsSerializer = SettingsSerializer(settingsDefaults.mapValues { it.value.key })

fun globalComponentContext(
    globalLifecycle: MutableUIComponentLifecycle,
    initialSettings: Settings,
    logger: CurrentPlatformLogger<LogLevel>,
    navigationRoot: NavigationRoot,
    deviceGameWordsProviderRegistry: DeviceGameWordsProviderRegistry,
): UIComponentContext = UIComponentContext {
    UIComponentLifecycleKey correspondsTo globalLifecycle
    
    Settings.Key correspondsTo KoneMutableAsynchronousHub(initialSettings)
    
    LoggerKey correspondsTo logger
    
    setUpNavigationControl(
        navigationRoot = navigationRoot,
        stringFormat = Json,
    )
    
    DeviceGameWordsProviderRegistryKey correspondsTo deviceGameWordsProviderRegistry
}

data class PagesDescription(
    val pageVariants: VariantsNode<MainWindowComponentConfiguration, MainWindowComponentChild, UIComponentContext>,
    val openPage: (page: MainWindowComponentConfiguration) -> Unit,
)

suspend fun UIComponentContext.pagesDescription(): PagesDescription {
    val coroutineScope = coroutineScope(Dispatchers.Default)
    
    val pageVariants =
        uiChildrenDefaultVariantsNode(
            loggerSource = "pagesDescription at dev.lounres.halfhat.client.ui.components.RealMainWindowComponent",
            navigationControllerSpec = NavigationControllerSpec(
                key = "page",
                configurationSerializer = MainWindowComponentConfiguration.serializer(),
                pathBuilder = { navigationState, children ->
                    val prefix = navigationState.currentVariant.path
                    val subPath = children[navigationState.currentVariant].context.navigationController?.pathBuilder?.invoke()
                    if (subPath != null)
                        NavigationNodePath(
                            path = KoneList.build {
                                +prefix
                                addAllFrom(subPath.path)
                            },
                            arguments = subPath.arguments
                        )
                    else
                        NavigationNodePath(
                            path = KoneList.of(prefix),
                            arguments = KoneMap.empty(),
                        )
                },
                restorationByPath = restorationByPath@ { path, childrenNode ->
                    if (path.path.isEmpty()) return@restorationByPath
                    childrenNode.navigate { allVariants, current ->
                        allVariants.firstThatOrNull { it.path == path.path[0u] } ?: current
                    }
                    val activeChild = childrenNode.hub.value.active
                    if (activeChild.configuration.path != path.path[0u]) return@restorationByPath
                    activeChild.componentContext.navigationController?.restorationByPath?.invoke(
                        NavigationNodePath(
                            path = path.path.drop(1u),
                            arguments = path.arguments,
                        )
                    )
                },
            ),
            allVariants = MainWindowComponentConfiguration.entries.toKoneList().toKoneSet(),
            initialVariant = this.settings.value[MainWindowComponentConfiguration.Key],
        ) { configuration, componentContext, navigation ->
            when (configuration) {
                MainWindowComponentConfiguration.Home ->
                    MainWindowComponentChild.Home(
                        RealHomePageComponent()
                    )
                MainWindowComponentConfiguration.Game ->
                    MainWindowComponentChild.Game(
                        RealGamePageComponent(
                            componentContext = componentContext,
                        )
                    )
                MainWindowComponentConfiguration.Miscellanea ->
                    MainWindowComponentChild.Miscellanea(
                        RealMiscellaneaComponent(
                            darkTheme = componentContext.settings.darkTheme,
                            volumeOn = componentContext.settings.volumeOn,
                            
                            openSettings = { /*TODO*/ },
                            openGameHistory = { /*TODO*/ },
                            openFeedback = { /*TODO*/ },
                            openRules = { /*TODO*/ },
                            openFAQ = { /*TODO*/ },
                            openAbout = { /*TODO*/ },
                            openNews = { /*TODO*/ },
                        )
                    )
            }
        }
    
    val openPage: (page: MainWindowComponentConfiguration) -> Unit = { page ->
        coroutineScope.launch {
            pageVariants.set(page)
        }
    }
    
    return PagesDescription(
        pageVariants = pageVariants,
        openPage = openPage,
    )
}