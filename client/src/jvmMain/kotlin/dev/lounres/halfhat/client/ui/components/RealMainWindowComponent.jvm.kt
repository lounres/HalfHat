package dev.lounres.halfhat.client.ui.components

import androidx.compose.ui.window.WindowState
import dev.lounres.halfhat.Language
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.newMutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.navigation.ChildrenVariants
import dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot
import dev.lounres.halfhat.client.logic.settings.language
import dev.lounres.halfhat.client.logic.settings.volumeOn
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderID
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderRegistry
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.hub.KoneAsynchronousHubView
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView


actual class RealMainWindowComponent(
    actual override val globalLifecycle: MutableUIComponentLifecycle,
    
    override val windowState: WindowState = WindowState(),
    override val onWindowCloseRequest: () -> Unit = {},
    
    actual override val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>,
    actual override val volumeOn: KoneMutableAsynchronousHubView<Boolean, *>,
    actual override val language: KoneMutableAsynchronousHubView<Language, *>,
    
    actual override val pageVariants: KoneAsynchronousHubView<ChildrenVariants<MainWindowComponentChild.Kind, MainWindowComponentChild, UIComponentContext>, *>,
    actual override val openPage: (page: MainWindowComponentChild.Kind) -> Unit,
    
    actual override val menuList: KoneAsynchronousHubView<KoneList<MainWindowComponentMenuItem>, *>,
): MainWindowComponent

suspend fun RealMainWindowComponent(
    deviceGameWordsProviderRegistry: DeviceGameWordsProviderRegistry,
    
    windowState: WindowState = WindowState(),
    onWindowCloseRequest: () -> Unit = {},
): RealMainWindowComponent {
    val globalLifecycle: MutableUIComponentLifecycle = newMutableUIComponentLifecycle()
    
    val navigationRoot = NavigationRoot { state, path -> /* TODO */ }
    
    val globalComponentContext = globalComponentContext(
        globalLifecycle = globalLifecycle,
        navigationRoot = navigationRoot,
        deviceGameWordsProviderRegistry = deviceGameWordsProviderRegistry,
        gameStateMachineWordsSource = GameStateMachine.WordsSource.Custom(DeviceGameWordsProviderID.Local("medium")),
    )
    
    val (pageVariants, openPage, menuList) = globalComponentContext.pagesDescription()
    
    return RealMainWindowComponent(
        globalLifecycle = globalLifecycle,
        
        windowState = windowState,
        onWindowCloseRequest = onWindowCloseRequest,
        
        darkTheme = globalComponentContext.settings.darkTheme,
        volumeOn = globalComponentContext.settings.volumeOn,
        language = globalComponentContext.settings.language,
        
        pageVariants = pageVariants.hub,
        openPage = openPage,
        
        menuList = menuList,
    )
}