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
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderID
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderRegistry
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderRegistryKey
import dev.lounres.halfhat.client.storage.settings.Settings
import dev.lounres.halfhat.client.storage.settings.SettingsSerializer
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.halfhat.client.ui.components.about.RealAboutPageComponent
import dev.lounres.halfhat.client.ui.components.faq.RealFAQPageComponent
import dev.lounres.halfhat.client.ui.components.feedback.RealFeedbackPageComponent
import dev.lounres.halfhat.client.ui.components.game.RealGamePageComponent
import dev.lounres.halfhat.client.ui.components.gameHistory.RealGameHistoryPageComponent
import dev.lounres.halfhat.client.ui.components.home.RealHomePageComponent
import dev.lounres.halfhat.client.ui.components.news.RealNewsPageComponent
import dev.lounres.halfhat.client.ui.components.rules.RealRulesPageComponent
import dev.lounres.halfhat.client.ui.components.settings.RealSettingsPageComponent
import dev.lounres.halfhat.client.ui.theming.DarkTheme
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
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.build
import dev.lounres.kone.collections.utils.drop
import dev.lounres.kone.collections.utils.firstThatOrNull
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.collections.utils.plusAssign
import dev.lounres.kone.hub.*
import dev.lounres.kone.registry.RegistryKey
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
    override val volumeOn: KoneMutableAsynchronousHubView<Boolean, *>
    override val language: KoneMutableAsynchronousHubView<Language, *>
    
    override val pageVariants: KoneAsynchronousHubView<ChildrenVariants<MainWindowComponentChild.Kind, MainWindowComponentChild, UIComponentContext>, *>
    override val openPage: (page: MainWindowComponentChild.Kind) -> Unit
    
    override val menuList: KoneAsynchronousHubView<KoneList<MainWindowComponentMenuItem>, *>
}

sealed interface RealMainWindowComponentMenuItemByKind {
    data object Separator: RealMainWindowComponentMenuItemByKind
    data class Child(val child: MainWindowComponentChild.Kind): RealMainWindowComponentMenuItemByKind
}

data class SettingDescription<T>(
    val key: RegistrySerializableKey<T>,
    val value: T,
)

expect val defaultDeviceGameWordsSource: GameStateMachine.WordsSource<DeviceGameWordsProviderID>

val settingsDefaults: Map<String, SettingDescription<*>> = mapOf(
    "DarkTheme" to SettingDescription(DarkTheme.Key, DarkTheme.System),
    "VolumeOn" to SettingDescription(VolumeOnKey, true),
    "Language" to SettingDescription(LanguageKey, Language.English),
    "InitialSelectedPage" to SettingDescription(MainWindowComponentChild.Kind.Key, MainWindowComponentChild.Kind.Primary.Game),
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
    savedSettings: Settings?,
    logger: CurrentPlatformLogger<LogLevel>,
    navigationRoot: NavigationRoot,
    deviceGameWordsProviderRegistry: DeviceGameWordsProviderRegistry,
): UIComponentContext = UIComponentContext {
    UIComponentLifecycleKey correspondsTo globalLifecycle
    
    Settings.Key correspondsTo KoneMutableAsynchronousHub(
        Settings {
            if (savedSettings != null) setFrom(savedSettings)
            @Suppress("UNCHECKED_CAST")
            for ((key, value) in settingsDefaults.values) if (key !in this) (key as RegistryKey<Any?>) correspondsTo value
        }
    )
    
    LoggerKey correspondsTo logger
    
    setUpNavigationControl(
        navigationRoot = navigationRoot,
        stringFormat = Json,
    )
    
    DeviceGameWordsProviderRegistryKey correspondsTo deviceGameWordsProviderRegistry
}

data class PagesDescription(
    val pageVariants: VariantsNode<MainWindowComponentChild.Kind, MainWindowComponentChild, UIComponentContext>,
    val openPage: (page: MainWindowComponentChild.Kind) -> Unit,
    val menuList: KoneAsynchronousHubView<KoneList<MainWindowComponentMenuItem>, *>,
)

suspend fun UIComponentContext.pagesDescription(): PagesDescription {
    val coroutineScope = coroutineScope(Dispatchers.Default)
    
    val pageVariants =
        uiChildrenDefaultVariantsNode(
            loggerSource = "pagesDescription at dev.lounres.halfhat.client.ui.components.RealMainWindowComponent",
            navigationControllerSpec = NavigationControllerSpec(
                key = "page",
                configurationSerializer = MainWindowComponentChild.Kind.serializer(),
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
            allVariants = KoneSet.build {
                +MainWindowComponentChild.Kind.Primary.entries.toKoneList()
                +MainWindowComponentChild.Kind.Secondary.entries.toKoneList()
            },
            initialVariant = this.settings.value[MainWindowComponentChild.Kind.Key],
        ) { configuration, componentContext, navigation ->
            when (configuration) {
                MainWindowComponentChild.Kind.Primary.Home ->
                    MainWindowComponentChild.Primary.Home(
                        RealHomePageComponent()
                    )
                MainWindowComponentChild.Kind.Primary.Game ->
                    MainWindowComponentChild.Primary.Game(
                        RealGamePageComponent(
                            componentContext = componentContext,
                        )
                    )
                MainWindowComponentChild.Kind.Secondary.News ->
                    MainWindowComponentChild.Secondary.News(
                        RealNewsPageComponent()
                    )
                MainWindowComponentChild.Kind.Secondary.Rules ->
                    MainWindowComponentChild.Secondary.Rules(
                        RealRulesPageComponent()
                    )
                MainWindowComponentChild.Kind.Secondary.FAQ ->
                    MainWindowComponentChild.Secondary.FAQ(
                        RealFAQPageComponent(
                            onFeedbackLinkClick = {
                                coroutineScope.launch {
                                    navigation.set(MainWindowComponentChild.Kind.Secondary.Feedback)
                                }
                            }
                        )
                    )
                MainWindowComponentChild.Kind.Secondary.GameHistory ->
                    MainWindowComponentChild.Secondary.GameHistory(
                        RealGameHistoryPageComponent()
                    )
                MainWindowComponentChild.Kind.Secondary.Settings ->
                    MainWindowComponentChild.Secondary.Settings(
                        RealSettingsPageComponent()
                    )
                MainWindowComponentChild.Kind.Secondary.Feedback ->
                    MainWindowComponentChild.Secondary.Feedback(
                        RealFeedbackPageComponent()
                    )
                MainWindowComponentChild.Kind.Secondary.About ->
                    MainWindowComponentChild.Secondary.About(
                        RealAboutPageComponent()
                    )
            }
        }
    
    val openPage: (page: MainWindowComponentChild.Kind) -> Unit = { page ->
        coroutineScope.launch {
            pageVariants.set(page)
        }
    }
    
    val menuListByKinds: KoneList<RealMainWindowComponentMenuItemByKind> = KoneList.build {
        this += MainWindowComponentChild.Kind.Primary.entries.toKoneList().map { RealMainWindowComponentMenuItemByKind.Child(it) }
        +RealMainWindowComponentMenuItemByKind.Separator
        this += MainWindowComponentChild.Kind.Secondary.entries.toKoneList().map { RealMainWindowComponentMenuItemByKind.Child(it) }
    }
    val menuList: KoneAsynchronousHub<KoneList<MainWindowComponentMenuItem>> =
        pageVariants.hub.map { childrenVariants ->
            menuListByKinds.map {
                when (it) {
                    is RealMainWindowComponentMenuItemByKind.Child -> MainWindowComponentMenuItem.Child(childrenVariants.allVariants[it.child])
                    RealMainWindowComponentMenuItemByKind.Separator -> MainWindowComponentMenuItem.Separator
                }
            }
        }
    
    return PagesDescription(
        pageVariants = pageVariants,
        openPage = openPage,
        menuList = menuList,
    )
}