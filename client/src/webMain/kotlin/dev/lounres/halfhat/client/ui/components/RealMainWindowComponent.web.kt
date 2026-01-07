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
import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodePath
import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeState
import dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot
import dev.lounres.halfhat.client.components.navigation.controller.navigationController
import dev.lounres.halfhat.client.components.navigation.controller.setUpNavigationControl
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultVariantsNode
import dev.lounres.halfhat.client.consts.WebPageSettings
import dev.lounres.halfhat.client.logic.settings.language
import dev.lounres.halfhat.client.logic.settings.volumeOn
import dev.lounres.halfhat.client.storage.settings.Settings
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.collections.interop.toKoneList
import dev.lounres.kone.collections.iterables.isEmpty
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.addAllFrom
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.list.empty
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.map.build
import dev.lounres.kone.collections.map.empty
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.map.isNotEmpty
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.build
import dev.lounres.kone.collections.utils.allIndexed
import dev.lounres.kone.collections.utils.drop
import dev.lounres.kone.collections.utils.firstThatOrNull
import dev.lounres.kone.collections.utils.joinToString
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.collections.utils.plusAssign
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView
import dev.lounres.kone.hub.map
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.registry.correspondsTo
import dev.lounres.kone.scope
import js.core.JsPrimitives.toKotlinString
import js.uri.encodeURIComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import web.events.EventHandler
import web.history.history
import web.location.location
import web.storage.localStorage
import web.url.URL
import web.window.window
import kotlin.js.JsString
import kotlin.js.toJsString


actual class RealMainWindowComponent(
    actual override val globalLifecycle: MutableUIComponentLifecycle,
    
    actual override val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>,
    actual override val volumeOn: KoneMutableAsynchronousHubView<Boolean, *>,
    actual override val language: KoneMutableAsynchronousHubView<Language, *>,
    
    actual override val pageVariants: KoneAsynchronousHub<ChildrenVariants<MainWindowComponentChild.Kind, MainWindowComponentChild, UIComponentContext>>,
    actual override val openPage: (page: MainWindowComponentChild.Kind) -> Unit,
    
    actual override val menuList: KoneAsynchronousHub<KoneList<MainWindowComponentMenuItem>>,
): MainWindowComponent

suspend fun RealMainWindowComponent(
//    localDictionariesRegistry: LocalDictionariesRegistry,
    
    initialSelectedPage: MainWindowComponentChild.Kind = MainWindowComponentChild.Kind.Primary.Game /* TODO: Page.Primary.Home */,
): RealMainWindowComponent {
    val globalLifecycle: MutableUIComponentLifecycle = newMutableUIComponentLifecycle()
    val navigationRoot = NavigationRoot { state, path ->
        history.pushState(
            data = Json.encodeToString(state).toJsString(),
            unused = "",
            url = path?.let { path ->
                val url = URL(location.href)
                url.pathname = WebPageSettings.base + path.path.joinToString(separator = "/")
                url.search = path.arguments.let {
                    if (it.isNotEmpty()) it.nodesView.joinToString(separator = "&") { node ->
                        "${encodeURIComponent(node.key)}=${encodeURIComponent(node.value)}"
                    } else ""
                }
                url
            }
        )
    }
    val settings = KoneMutableAsynchronousHub(
        Settings {
            localStorage.getItem("settings")?.let { setFrom(Json.decodeFromString(settingsSerializer, it)) }
            @Suppress("UNCHECKED_CAST")
            for ((key, value) in settingsDefaults.values) if (key !in this) (key as RegistryKey<Any?>) correspondsTo value
        }
    )
    settings.subscribe {
        localStorage.setItem("settings", Json.encodeToString(settingsSerializer, it))
    }
    val globalComponentContext = UIComponentContext {
        UIComponentLifecycleKey correspondsTo globalLifecycle
        LoggerKey correspondsTo logger
        @Suppress("JSON_FORMAT_REDUNDANT_DEFAULT")
        setUpNavigationControl(
            navigationRoot = navigationRoot,
            stringFormat = Json,
        )
        
        DeviceGameWordsProviderRegistryKey correspondsTo DeviceGameWordsProviderRegistry
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
    val coroutineScope = globalComponentContext.coroutineScope(Dispatchers.Default)
    
    val pageVariants =
        globalComponentContext.uiChildrenDefaultVariantsNode(
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
    
    globalComponentContext.navigationController?.setPathBuilder {
        pageVariants.context.navigationController?.pathBuilder?.invoke() ?: NavigationNodePath(
            path = KoneList.empty(),
            arguments = KoneMap.empty(),
        )
    }
    
    globalComponentContext.navigationController?.setRestorationByPath {
        pageVariants.context.navigationController?.restorationByPath?.invoke(it)
    }
    
    @Suppress("JSON_FORMAT_REDUNDANT_DEFAULT")
    val json = Json {
        serializersModule = SerializersModule {
        
        }
    }
    
    scope {
        val locationPath = location.pathname
        val actualPath = locationPath.split('/').filter { it.isNotEmpty() }.toKoneList()
        val basePath = WebPageSettings.base.split('/').filter { it.isNotEmpty() }.toKoneList()
        if (actualPath.size >= basePath.size && basePath.allIndexed { index, value -> actualPath[index] == value })
            navigationRoot.restoreByPath(
                NavigationNodePath(
                    path = actualPath.drop(basePath.size),
                    arguments = KoneMap.build {
                        // FIXME: kotlinx-wrappers #2824
//                        for ((key, value) in URLSearchParams(location.search).entries()) {
//                            set(key.toKotlinString(), value.toKotlinString())
//                        }
                    }
                )
            )
    }
    
    history.replaceState(
        data = Json.encodeToString(navigationRoot.getState()).toJsString(),
        unused = "",
        url = navigationRoot.getPath()?.let { URL(WebPageSettings.base + it.path.joinToString(separator = "/"), location.origin) }
    )
    
    window.onpopstate = EventHandler { event ->
        val state = try {
            (event.state as? JsString)?.toKotlinString()?.let { json.decodeFromString<NavigationNodeState>(it) }
        } catch (e: SerializationException) { null }
        if (state != null) coroutineScope.launch {
            navigationRoot.restore(state)
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
        
        darkTheme = settings.darkTheme,
        volumeOn = settings.volumeOn,
        language = settings.language,
        
        pageVariants = pageVariants.hub,
        openPage = openPage,
        
        menuList = menuList,
    )
}