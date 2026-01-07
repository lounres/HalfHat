package dev.lounres.halfhat.client.ui.components

import androidx.compose.ui.window.WindowState
import dev.lounres.halfhat.Language
import dev.lounres.halfhat.client.logic.settings.DeviceGameDefaultSettingsKey
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderID
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderRegistry
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderRegistryKey
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.ui.components.about.RealAboutPageComponent
import dev.lounres.halfhat.client.ui.components.faq.RealFAQPageComponent
import dev.lounres.halfhat.client.ui.components.feedback.RealFeedbackPageComponent
import dev.lounres.halfhat.client.ui.components.game.RealGamePageComponent
import dev.lounres.halfhat.client.ui.components.gameHistory.RealGameHistoryPageComponent
import dev.lounres.halfhat.client.ui.components.home.RealHomePageComponent
import dev.lounres.halfhat.client.ui.components.news.RealNewsPageComponent
import dev.lounres.halfhat.client.ui.components.rules.RealRulesPageComponent
import dev.lounres.halfhat.client.ui.components.settings.RealSettingsPageComponent
import dev.lounres.halfhat.client.utils.logger
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleKey
import dev.lounres.halfhat.client.components.lifecycle.newMutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.logger.LoggerKey
import dev.lounres.halfhat.client.components.navigation.ChildrenVariants
import dev.lounres.halfhat.client.components.navigation.NavigationControllerSpec
import dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot
import dev.lounres.halfhat.client.components.navigation.controller.setUpNavigationControl
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultVariantsNode
import dev.lounres.halfhat.client.logic.settings.language
import dev.lounres.halfhat.client.logic.settings.volumeOn
import dev.lounres.halfhat.client.storage.settings.Settings
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.collections.interop.toKoneList
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.build
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.collections.utils.plusAssign
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView
import dev.lounres.kone.hub.map
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.registry.correspondsTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


actual class RealMainWindowComponent(
    actual override val globalLifecycle: MutableUIComponentLifecycle,
    
    override val windowState: WindowState = WindowState(),
    override val onWindowCloseRequest: () -> Unit = {},
    
    actual override val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>,
    actual override val volumeOn: KoneMutableAsynchronousHubView<Boolean, *>,
    actual override val language: KoneMutableAsynchronousHubView<Language, *>,
    
    actual override val pageVariants: KoneAsynchronousHub<ChildrenVariants<MainWindowComponentChild.Kind, MainWindowComponentChild, UIComponentContext>>,
    actual override val openPage: (page: MainWindowComponentChild.Kind) -> Unit,
    
    actual override val menuList: KoneAsynchronousHub<KoneList<MainWindowComponentMenuItem>>,
): MainWindowComponent

suspend fun RealMainWindowComponent(
    deviceGameWordsProviderRegistry: DeviceGameWordsProviderRegistry,
    
    windowState: WindowState = WindowState(),
    onWindowCloseRequest: () -> Unit = {},
    
    initialSelectedPage: MainWindowComponentChild.Kind = MainWindowComponentChild.Kind.Primary.Game /* TODO: Page.Primary.Home */,
): RealMainWindowComponent {
    val globalLifecycle: MutableUIComponentLifecycle = newMutableUIComponentLifecycle()
    val navigationRoot = NavigationRoot { state, path -> /* TODO */ }
    val settings = KoneMutableAsynchronousHub(
        Settings {
//            setFrom(...)
            @Suppress("UNCHECKED_CAST")
            for ((key, value) in settingsDefaults.values) if (key !in this) (key as RegistryKey<Any?>) correspondsTo value
        }
    )
    val globalComponentContext = UIComponentContext {
        UIComponentLifecycleKey correspondsTo globalLifecycle
        LoggerKey correspondsTo logger
        @Suppress("JSON_FORMAT_REDUNDANT_DEFAULT")
        setUpNavigationControl(
            navigationRoot = navigationRoot,
            stringFormat = Json,
        )
        
        DeviceGameWordsProviderRegistryKey correspondsTo deviceGameWordsProviderRegistry
        Settings.Key correspondsTo settings
        // TODO: Move the following to settings.
        DeviceGameDefaultSettingsKey correspondsTo KoneMutableAsynchronousHub(
            GameStateMachine.GameSettings.Builder<DeviceGameWordsProviderID>(
                preparationTimeSeconds = 3u,
                explanationTimeSeconds = 20u,
                finalGuessTimeSeconds = 3u,
                strictMode = false,
                cachedEndConditionWordsNumber = 100u,
                cachedEndConditionCyclesNumber = 10u,
                gameEndConditionType = GameStateMachine.GameEndCondition.Type.Words,
                wordsSource = GameStateMachine.WordsSource.Custom(DeviceGameWordsProviderID.Local("medium"))
            )
        )
    }
    
    val pageVariants =
        globalComponentContext.uiChildrenDefaultVariantsNode(
            navigationControllerSpec = NavigationControllerSpec(
                key = "page",
                configurationSerializer = MainWindowComponentChild.Kind.serializer(),
            ),
            allVariants = KoneSet.build {
                +MainWindowComponentChild.Kind.Primary.entries.toKoneList()
                +MainWindowComponentChild.Kind.Secondary.entries.toKoneList()
            },
            initialVariant = initialSelectedPage,
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
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.set(MainWindowComponentChild.Kind.Secondary.FAQ)
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
        CoroutineScope(Dispatchers.Default).launch {
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
    
    return RealMainWindowComponent(
        globalLifecycle = globalLifecycle,
        
        windowState = windowState,
        onWindowCloseRequest = onWindowCloseRequest,
        
        darkTheme = settings.darkTheme,
        volumeOn = settings.volumeOn,
        language = settings.language,
        
        pageVariants = pageVariants.hub,
        openPage = openPage,
        
        menuList = menuList,
    )
}