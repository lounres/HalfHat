package dev.lounres.halfhat.client.ui.components

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
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleKey
import dev.lounres.halfhat.client.components.lifecycle.newMutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.logger.LoggerKey
import dev.lounres.halfhat.client.components.navigation.ChildrenVariants
import dev.lounres.halfhat.client.components.navigation.NavigationControllerSpec
import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeState
import dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot
import dev.lounres.halfhat.client.components.navigation.controller.doStoringNavigation
import dev.lounres.halfhat.client.components.navigation.controller.navigationContext
import dev.lounres.halfhat.client.components.navigation.controller.setUpNavigationControl
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultVariantsNode
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
import dev.lounres.kone.hub.map
import dev.lounres.kone.registry.correspondsTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import web.events.EventType
import web.events.addEventListener
import web.history.PopStateEvent
import web.history.history
import web.window.window
import kotlin.js.JsString
import kotlin.js.toJsString


class RealMainWindowComponent(
    override val globalLifecycle: MutableUIComponentLifecycle,
    
    override val volumeOn: MutableStateFlow<Boolean>,
    override val language: MutableStateFlow<Language>,
    
    override val pageVariants: KoneAsynchronousHub<ChildrenVariants<MainWindowComponent.Child.Kind, MainWindowComponent.Child>>,
    override val openPage: (page: MainWindowComponent.Child.Kind) -> Unit,
    
    override val menuList: KoneAsynchronousHub<KoneList<MainWindowComponent.MenuItem>>,
): MainWindowComponent {
    
    sealed interface MenuItemByKind {
        data object Separator: MenuItemByKind
        data class Child(val child: MainWindowComponent.Child.Kind): MenuItemByKind
    }
}

suspend fun RealMainWindowComponent(
//    localDictionariesRegistry: LocalDictionariesRegistry,
    
    initialVolumeOn: Boolean = true,
    initialLanguage: Language = Language.English,
    
    initialSelectedPage: MainWindowComponent.Child.Kind = MainWindowComponent.Child.Kind.Primary.Game /* TODO: Page.Primary.Home */,
): RealMainWindowComponent {
    val globalLifecycle: MutableUIComponentLifecycle = newMutableUIComponentLifecycle()
    val navigationRoot = NavigationRoot { state ->
        history.pushState(
            data = Json.encodeToString(state).toJsString(),
            unused = "",
        )
    }
    val globalComponentContext = UIComponentContext {
        UIComponentLifecycleKey correspondsTo globalLifecycle
        LoggerKey correspondsTo logger
        DeviceGameWordsProviderRegistryKey correspondsTo DeviceGameWordsProviderRegistry
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
        @Suppress("JSON_FORMAT_REDUNDANT_DEFAULT")
        setUpNavigationControl(
            navigationRoot = navigationRoot,
            stringFormat = Json,
        )
    }
    val coroutineScope = globalComponentContext.coroutineScope(Dispatchers.Default)
    
    val volumeOn: MutableStateFlow<Boolean> = MutableStateFlow(initialVolumeOn)
    val language: MutableStateFlow<Language> = MutableStateFlow(initialLanguage)
    
    val pageVariants =
        globalComponentContext.uiChildrenDefaultVariantsNode(
            navigationControllerSpec = NavigationControllerSpec(
                key = "page",
                configurationSerializer = MainWindowComponent.Child.Kind.serializer(),
            ),
            allVariants = KoneSet.build {
                +MainWindowComponent.Child.Kind.Primary.entries.toKoneList()
                +MainWindowComponent.Child.Kind.Secondary.entries.toKoneList()
            },
            initialVariant = initialSelectedPage,
        ) { configuration, componentContext, navigation ->
            when (configuration) {
                MainWindowComponent.Child.Kind.Primary.Home ->
                    MainWindowComponent.Child.Primary.Home(
                        RealHomePageComponent()
                    )
                MainWindowComponent.Child.Kind.Primary.Game ->
                    MainWindowComponent.Child.Primary.Game(
                        RealGamePageComponent(
                            componentContext = componentContext,
                            volumeOn = volumeOn
                        )
                    )
                MainWindowComponent.Child.Kind.Secondary.News ->
                    MainWindowComponent.Child.Secondary.News(
                        RealNewsPageComponent()
                    )
                MainWindowComponent.Child.Kind.Secondary.Rules ->
                    MainWindowComponent.Child.Secondary.Rules(
                        RealRulesPageComponent()
                    )
                MainWindowComponent.Child.Kind.Secondary.FAQ ->
                    MainWindowComponent.Child.Secondary.FAQ(
                        RealFAQPageComponent(
                            onFeedbackLinkClick = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    componentContext.navigationContext.doStoringNavigation {
                                        navigation.set(MainWindowComponent.Child.Kind.Secondary.FAQ)
                                    }
                                }
                            }
                        )
                    )
                MainWindowComponent.Child.Kind.Secondary.GameHistory ->
                    MainWindowComponent.Child.Secondary.GameHistory(
                        RealGameHistoryPageComponent()
                    )
                MainWindowComponent.Child.Kind.Secondary.Settings ->
                    MainWindowComponent.Child.Secondary.Settings(
                        RealSettingsPageComponent()
                    )
                MainWindowComponent.Child.Kind.Secondary.Feedback ->
                    MainWindowComponent.Child.Secondary.Feedback(
                        RealFeedbackPageComponent()
                    )
                MainWindowComponent.Child.Kind.Secondary.About ->
                    MainWindowComponent.Child.Secondary.About(
                        RealAboutPageComponent()
                    )
            }
        }
    
    @Suppress("JSON_FORMAT_REDUNDANT_DEFAULT")
    val json = Json {
        serializersModule = SerializersModule {
        
        }
    }
    
    history.replaceState(
        data = Json.encodeToString(navigationRoot.getState()).toJsString(),
        unused = "",
    )
    
    window.addEventListener(
        EventType<PopStateEvent>("popstate"),
        { event ->
            val state = try {
                (event.state as? JsString)?.toString()?.let { json.decodeFromString<NavigationNodeState>(it) }
            } catch (e: SerializationException) { null }
            if (state != null) coroutineScope.launch {
                navigationRoot.restore(state)
            }
        },
    )
    
    val openPage: (page: MainWindowComponent.Child.Kind) -> Unit = { page ->
        CoroutineScope(Dispatchers.Default).launch {
            globalComponentContext.navigationContext.doStoringNavigation {
                pageVariants.set(page)
            }
        }
    }
    
    val menuListByKinds: KoneList<RealMainWindowComponent.MenuItemByKind> = KoneList.build {
        this += MainWindowComponent.Child.Kind.Primary.entries.toKoneList().map { RealMainWindowComponent.MenuItemByKind.Child(it) }
        +RealMainWindowComponent.MenuItemByKind.Separator
        this += MainWindowComponent.Child.Kind.Secondary.entries.toKoneList().map { RealMainWindowComponent.MenuItemByKind.Child(it) }
    }
    val menuList: KoneAsynchronousHub<KoneList<MainWindowComponent.MenuItem>> =
        pageVariants.hub.map { childrenVariants ->
            menuListByKinds.map {
                when (it) {
                    is RealMainWindowComponent.MenuItemByKind.Child -> MainWindowComponent.MenuItem.Child(childrenVariants.allVariants[it.child])
                    RealMainWindowComponent.MenuItemByKind.Separator -> MainWindowComponent.MenuItem.Separator
                }
            }
        }
    
    return RealMainWindowComponent(
        globalLifecycle = globalLifecycle,
        
        volumeOn = volumeOn,
        language = language,
        
        pageVariants = pageVariants.hub,
        openPage = openPage,
        
        menuList = menuList,
    )
}