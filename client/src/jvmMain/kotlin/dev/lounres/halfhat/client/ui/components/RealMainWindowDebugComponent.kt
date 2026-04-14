package dev.lounres.halfhat.client.ui.components

import androidx.compose.ui.window.WindowState
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.newMutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.navigation.controller.NavigationLoggerSpec
import dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot
import dev.lounres.halfhat.client.components.navigation.controller.navigationController
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderRegistry
import dev.lounres.halfhat.client.storage.settings.Settings
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.scope
import dev.lounres.logKube.core.DefaultCurrentPlatformLogWriter
import dev.lounres.logKube.core.LogAcceptor
import dev.lounres.logKube.core.Logger


class RealMainWindowDebugComponent(
    override val darkTheme: KoneMutableAsynchronousHub<DarkTheme>,
    override val mainComponents: KoneList<MainWindowComponent>,
) : MainWindowDebugComponent

suspend fun RealMainWindowDebugComponent(
    initialSettings: Settings,
    
    windowStates: KoneList<WindowState>,
    onWindowCloseRequest: () -> Unit = {},
): RealMainWindowDebugComponent {
    val deviceGameWordsProviderRegistry = DeviceGameWordsProviderRegistry
    
    val globalLifecycle: MutableUIComponentLifecycle = newMutableUIComponentLifecycle()
    
    val logger = Logger(
        name = "Desktop HalfHat application logger",
        LogAcceptor(DefaultCurrentPlatformLogWriter) { false },
    )
    
    val navigationRoot = NavigationRoot(
        loggerSpec = NavigationLoggerSpec(
            logger = logger,
            loggerSource = "dev.lounres.halfhat.client.ui.components.RealMainWindowComponent"
        )
    ) { action, state, path -> /* TODO */ }
    
    val globalComponentContext = globalComponentContext(
        globalLifecycle = globalLifecycle,
        initialSettings = initialSettings,
        logger = logger,
        navigationRoot = navigationRoot,
        deviceGameWordsProviderRegistry = deviceGameWordsProviderRegistry,
    )
    
    globalComponentContext.settings.subscribe {
        // TODO
    }

//    val coroutineScope = globalComponentContext.coroutineScope(Dispatchers.Default)
    
    val (pageVariants, openPage) = globalComponentContext.pagesDescription()

//    globalComponentContext.navigationController?.setPathBuilder {
//        // TODO
//    }
    
    globalComponentContext.navigationController?.setRestorationByPath {
        pageVariants.context.navigationController?.restorationByPath?.invoke(it)
    }
    
    scope {
        // TODO: Add restoration by initial path and outer URI calls
    }
    
    return RealMainWindowDebugComponent(
        darkTheme = globalComponentContext.settings.darkTheme,
        mainComponents = windowStates.map {
            RealMainWindowComponent(
                globalLifecycle = globalLifecycle,
                
                windowState = it,
                onWindowCloseRequest = onWindowCloseRequest,
                
                darkTheme = globalComponentContext.settings.darkTheme,
                
                pageVariants = pageVariants.hub,
                openPage = openPage,
            )
        }
    )
}