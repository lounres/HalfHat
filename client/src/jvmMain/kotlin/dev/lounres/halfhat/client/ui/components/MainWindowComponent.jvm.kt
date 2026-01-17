package dev.lounres.halfhat.client.ui.components

import androidx.compose.ui.window.WindowState
import dev.lounres.halfhat.Language
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.navigation.ChildrenVariants
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.hub.KoneAsynchronousHubView
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView


actual interface MainWindowComponent {
    val onWindowCloseRequest: () -> Unit
    
    actual val globalLifecycle: MutableUIComponentLifecycle
    val windowState: WindowState
    
    actual val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>
    
    actual val pageVariants: KoneAsynchronousHubView<ChildrenVariants<MainWindowComponentConfiguration, MainWindowComponentChild, UIComponentContext>, *>
    actual val openPage: (page: MainWindowComponentConfiguration) -> Unit
}