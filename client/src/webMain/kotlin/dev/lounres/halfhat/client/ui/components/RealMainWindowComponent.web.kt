package dev.lounres.halfhat.client.ui.components

import dev.lounres.halfhat.Language
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.newMutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.navigation.ChildrenVariants
import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodePath
import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeState
import dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot
import dev.lounres.halfhat.client.components.navigation.controller.navigationController
import dev.lounres.halfhat.client.consts.WebPageSettings
import dev.lounres.halfhat.client.logic.settings.language
import dev.lounres.halfhat.client.logic.settings.volumeOn
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderID
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderRegistry
import dev.lounres.halfhat.client.storage.settings.Settings
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.interop.toKoneList
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.empty
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.map.build
import dev.lounres.kone.collections.map.empty
import dev.lounres.kone.collections.map.isNotEmpty
import dev.lounres.kone.collections.utils.allIndexed
import dev.lounres.kone.collections.utils.drop
import dev.lounres.kone.collections.utils.joinToString
import dev.lounres.kone.hub.KoneAsynchronousHubView
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView
import dev.lounres.kone.scope
import js.core.JsPrimitives.toKotlinString
import js.uri.encodeURIComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
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
    
    actual override val pageVariants: KoneAsynchronousHubView<ChildrenVariants<MainWindowComponentChild.Kind, MainWindowComponentChild, UIComponentContext>, *>,
    actual override val openPage: (page: MainWindowComponentChild.Kind) -> Unit,
    
    actual override val menuList: KoneAsynchronousHubView<KoneList<MainWindowComponentMenuItem>, *>,
): MainWindowComponent

actual val defaultDeviceGameWordsSource: GameStateMachine.WordsSource<DeviceGameWordsProviderID> =
    GameStateMachine.WordsSource.Custom(DeviceGameWordsProviderID.Local("medium"))

suspend fun RealMainWindowComponent(
//    localDictionariesRegistry: LocalDictionariesRegistry,
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
                    if (it.isNotEmpty()) it.nodesView.joinToString(prefix = "?", separator = "&") { node ->
                        "${encodeURIComponent(node.key)}=${encodeURIComponent(node.value)}"
                    } else ""
                }
                url
            }
        )
    }
    
    val globalComponentContext = globalComponentContext(
        globalLifecycle = globalLifecycle,
        navigationRoot = navigationRoot,
        savedSettings = localStorage.getItem("settings")?.let { Json.decodeFromString(settingsSerializer, it) },
        deviceGameWordsProviderRegistry = DeviceGameWordsProviderRegistry,
    )
    
    globalComponentContext.settings.subscribe {
        localStorage.setItem("settings", Json.encodeToString(settingsSerializer, it))
    }
    
    val coroutineScope = globalComponentContext.coroutineScope(Dispatchers.Default)
    
    val (pageVariants, openPage, menuList) = globalComponentContext.pagesDescription()
    
    globalComponentContext.navigationController?.setPathBuilder {
        pageVariants.context.navigationController?.pathBuilder?.invoke() ?: NavigationNodePath(
            path = KoneList.empty(),
            arguments = KoneMap.empty(),
        )
    }
    
    globalComponentContext.navigationController?.setRestorationByPath {
        pageVariants.context.navigationController?.restorationByPath?.invoke(it)
    }
    
    scope {
        val state = try {
            (history.state as? JsString)?.toKotlinString()?.let { Json.decodeFromString<NavigationNodeState>(it) }
        } catch (e: SerializationException) { null }
        if (state != null) {
            navigationRoot.restore(state)
            return@scope
        }
        
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
            (event.state as? JsString)?.toKotlinString()?.let { Json.decodeFromString<NavigationNodeState>(it) }
        } catch (e: SerializationException) { null }
        if (state != null) coroutineScope.launch {
            navigationRoot.restore(state)
        }
    }
    
    return RealMainWindowComponent(
        globalLifecycle = globalLifecycle,
        
        darkTheme = globalComponentContext.settings.darkTheme,
        volumeOn = globalComponentContext.settings.volumeOn,
        language = globalComponentContext.settings.language,
        
        pageVariants = pageVariants.hub,
        openPage = openPage,
        
        menuList = menuList,
    )
}