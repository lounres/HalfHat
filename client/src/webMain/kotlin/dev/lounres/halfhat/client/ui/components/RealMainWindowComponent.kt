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
import dev.lounres.kone.hub.map
import dev.lounres.kone.registry.correspondsTo
import dev.lounres.kone.scope
import js.array.component1
import js.array.component2
import js.core.JsPrimitives.toKotlinString
import js.iterable.iterator
import js.uri.encodeURIComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import web.console.console
import web.events.EventHandler
import web.history.history
import web.location.location
import web.url.URL
import web.url.URLSearchParams
import web.window.window
import kotlin.js.JsString
import kotlin.js.toJsString


class RealMainWindowComponent(
    override val globalLifecycle: MutableUIComponentLifecycle,
    
    override val volumeOn: MutableStateFlow<Boolean>,
    override val language: MutableStateFlow<Language>,
    
    override val pageVariants: KoneAsynchronousHub<ChildrenVariants<MainWindowComponent.Child.Kind, MainWindowComponent.Child, UIComponentContext>>,
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
    val navigationRoot = NavigationRoot { state, path ->
        println(URL(WebPageSettings.base + (path?.path?.joinToString(separator = "/") ?: ""), location.origin))
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
                                    navigation.set(MainWindowComponent.Child.Kind.Secondary.FAQ)
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
    
    val openPage: (page: MainWindowComponent.Child.Kind) -> Unit = { page ->
        CoroutineScope(Dispatchers.Default).launch {
            pageVariants.set(page)
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